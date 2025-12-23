package com.example.jian2.ui.diary.write

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WriteDiaryFragment : Fragment(R.layout.fragment_write_diary) {

    private val viewModel: DiaryViewModel by activityViewModels()

    private var selectedCoverUri: String? = null

    private val pickCoverLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedCoverUri = uri.toString()
            view?.findViewById<ImageView>(R.id.ivCoverPreview)?.apply {
                visibility = View.VISIBLE
                load(uri) { crossfade(true) }
            }
        }
    }

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
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val btnPickCover = view.findViewById<MaterialButton>(R.id.btnPickCover)
        val ivCoverPreview = view.findViewById<ImageView>(R.id.ivCoverPreview)

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
        btnSave.text = if (isEdit) "保存修改" else "保存"

        btnPickCover.setOnClickListener {
            pickCoverLauncher.launch("image/*")
        }

        // 编辑模式：回填
        if (isEdit) {
            viewModel.loadDiaryById(diaryId)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.editing.collectLatest { e ->
                    if (e != null && e.id == diaryId) {
                        etTitle.setText(e.title)
                        etContent.setText(e.content)
                        etTags.setText(e.tagsText)
                        seekMood.progress = e.mood
                        updateMoodText(e.mood)

                        selectedCoverUri = e.coverUri
                        if (!selectedCoverUri.isNullOrBlank()) {
                            ivCoverPreview.visibility = View.VISIBLE
                            ivCoverPreview.load(selectedCoverUri) { crossfade(true) }
                        } else {
                            ivCoverPreview.visibility = View.GONE
                        }
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val tags = etTags.text.toString().trim()
            val mood = seekMood.progress.coerceIn(0, 5)

            if (title.isEmpty()) { toast("标题不能为空"); return@setOnClickListener }
            if (content.isEmpty()) { toast("正文不能为空"); return@setOnClickListener }

            if (!isEdit) {
                viewModel.addDiary(title, content, mood, tags, selectedCoverUri)
                toast("已保存")
                parentFragmentManager.popBackStack()
            } else {
                val e = viewModel.editing.value
                if (e == null || e.id != diaryId) {
                    toast("编辑数据尚未加载完成，请稍后再试")
                    return@setOnClickListener
                }
                viewModel.updateDiary(
                    id = e.id,
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tags,
                    coverUri = selectedCoverUri,
                    isPinned = e.isPinned,
                    createdAt = e.createdAt
                )
                toast("已更新")
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
