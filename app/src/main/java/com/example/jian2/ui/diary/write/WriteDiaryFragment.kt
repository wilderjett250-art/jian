package com.example.jian2.ui.diary.write

import android.os.Bundle
import android.view.View
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

class WriteDiaryFragment : Fragment(R.layout.fragment_write_diary) {

    private val viewModel: DiaryViewModel by activityViewModels()

    companion object {
        private const val ARG_DIARY_ID = "arg_diary_id"

        fun newInstanceCreate() = WriteDiaryFragment()
        fun newInstanceEdit(id: Long) = WriteDiaryFragment().apply {
            arguments = Bundle().apply { putLong(ARG_DIARY_ID, id) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val seekMood = view.findViewById<SeekBar>(R.id.seekMood)
        val tvMood = view.findViewById<TextView>(R.id.tvMood)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        fun updateMoodText(v: Int) { tvMood.text = "心情：$v" }
        updateMoodText(seekMood.progress)

        seekMood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateMoodText(progress)
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val diaryId = arguments?.getLong(ARG_DIARY_ID, 0L) ?: 0L
        val isEdit = diaryId != 0L

        if (isEdit) {
            viewModel.loadDiaryById(diaryId)
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
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val mood = seekMood.progress

            if (title.isEmpty()) { toast("标题不能为空"); return@setOnClickListener }
            if (content.isEmpty()) { toast("正文不能为空"); return@setOnClickListener }

            if (!isEdit) viewModel.addDiary(title, content, mood)
            else viewModel.updateDiary(diaryId, title, content, mood)

            parentFragmentManager.popBackStack()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
