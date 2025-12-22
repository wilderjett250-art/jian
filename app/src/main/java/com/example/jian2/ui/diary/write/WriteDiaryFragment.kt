package com.example.jian2.ui.diary.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WriteDiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_write_diary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val seekMood = view.findViewById<SeekBar>(R.id.seekMood)

        // ✅ 你的布局里是 tvMood，不是 tvMoodValue
        val tvMood = view.findViewById<TextView>(R.id.tvMood)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        val diaryId = arguments?.getLong(ARG_DIARY_ID, 0L) ?: 0L
        val isEdit = diaryId != 0L

        fun updateMoodText(v: Int) {
            tvMood.text = "心情：$v"
        }

        updateMoodText(seekMood.progress)

        seekMood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateMoodText(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        if (isEdit) {
            btnSave.text = "保存修改"
            viewModel.loadDiaryById(diaryId)

            // 监听 editing 回填
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.editing.collectLatest { e ->
                    if (e != null && e.id == diaryId) {
                        etTitle.setText(e.title)
                        etContent.setText(e.content)
                        seekMood.progress = e.mood
                        updateMoodText(e.mood)
                    }
                }
            }
        } else {
            btnSave.text = "保存"
            // ✅ 不再调用 clearEditing（你项目里没有这个函数）
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val mood = seekMood.progress

            if (title.isEmpty()) {
                toast("标题不能为空")
                return@setOnClickListener
            }
            if (content.isEmpty()) {
                toast("正文不能为空")
                return@setOnClickListener
            }

            if (!isEdit) {
                // ✅ 只用你已有的 addDiary(title, content, mood)
                viewModel.addDiary(title, content, mood)
                toast("已保存")
            } else {
                // ✅ 只用你已有的 updateDiary(id, title, content, mood)
                viewModel.updateDiary(diaryId, title, content, mood)
                toast("已更新")
            }

            parentFragmentManager.popBackStack()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_DIARY_ID = "arg_diary_id"

        fun newInstanceEdit(diaryId: Long): WriteDiaryFragment {
            return WriteDiaryFragment().apply {
                arguments = Bundle().apply { putLong(ARG_DIARY_ID, diaryId) }
            }
        }

        fun newInstanceCreate(): WriteDiaryFragment = WriteDiaryFragment()
    }
}
