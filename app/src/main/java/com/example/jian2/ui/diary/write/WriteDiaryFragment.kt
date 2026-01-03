package com.example.jian2.ui.diary.write

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WriteDiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    private var coverUriStr: String? = null
    private var audioUriStr: String? = null
    private var videoUriStr: String? = null

    private var editId: Long = -1L

    companion object {
        private const val ARG_EDIT_ID = "edit_id"

        fun newInstanceEdit(id: Long): WriteDiaryFragment {
            return WriteDiaryFragment().apply {
                arguments = bundleOf(ARG_EDIT_ID to id)
            }
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                persistReadPermission(uri)
                coverUriStr = uri.toString()
                view?.findViewById<ImageView>(R.id.ivCover)?.apply {
                    visibility = View.VISIBLE
                    setImageURI(uri)
                }
            }
        }

    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                persistReadPermission(uri)
                audioUriStr = uri.toString()
                view?.findViewById<TextView>(R.id.tvAudioHint)?.text =
                    "已选择音频：${uri.lastPathSegment ?: uri}"
            }
        }

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                persistReadPermission(uri)
                videoUriStr = uri.toString()
                view?.findViewById<TextView>(R.id.tvVideoHint)?.text =
                    "已选择视频：${uri.lastPathSegment ?: uri}"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_write_diary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editId = arguments?.getLong(ARG_EDIT_ID, -1L) ?: -1L

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val moodBar = view.findViewById<SeekBar>(R.id.seekMood)

        val btnPickCover = view.findViewById<MaterialButton>(R.id.btnPickCover)
        val btnPickAudio = view.findViewById<MaterialButton>(R.id.btnPickAudio)
        val btnPickVideo = view.findViewById<MaterialButton>(R.id.btnPickVideo)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        btnPickCover.setOnClickListener { pickImageLauncher.launch(arrayOf("image/*")) }
        btnPickAudio.setOnClickListener { pickAudioLauncher.launch(arrayOf("audio/*")) }
        btnPickVideo.setOnClickListener { pickVideoLauncher.launch(arrayOf("video/*")) }

        // ====== 编辑模式：回填 ======
        if (editId > 0) {
            viewModel.loadDiaryById(editId)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.editing.collect { e ->
                    if (e == null || e.id != editId) return@collect

                    etTitle.setText(e.title)
                    etContent.setText(e.content)
                    etTags.setText(e.tagsText)

                    // moodBar 取值与你实体 mood 对齐（你项目里是 0~5）
                    moodBar.progress = e.mood

                    coverUriStr = e.coverUri
                    audioUriStr = e.audioUri
                    videoUriStr = e.videoUri

                    // UI 提示
                    if (!coverUriStr.isNullOrBlank()) {
                        view.findViewById<ImageView>(R.id.ivCover)?.apply {
                            visibility = View.VISIBLE
                            setImageURI(Uri.parse(coverUriStr))
                        }
                    }
                    if (!audioUriStr.isNullOrBlank()) {
                        view.findViewById<TextView>(R.id.tvAudioHint)?.text = "已选择音频：${Uri.parse(audioUriStr).lastPathSegment ?: audioUriStr}"
                    }
                    if (!videoUriStr.isNullOrBlank()) {
                        view.findViewById<TextView>(R.id.tvVideoHint)?.text = "已选择视频：${Uri.parse(videoUriStr).lastPathSegment ?: videoUriStr}"
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val mood = moodBar.progress
            val tags = etTags.text.toString().trim().ifBlank { null }

            if (editId > 0) {
                // ✅ 编辑：不影响新增逻辑
                viewModel.updateDiary(
                    id = editId,
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tags,
                    coverUri = coverUriStr,
                    audioUri = audioUriStr,
                    videoUri = videoUriStr
                )
            } else {
                // ✅ 新增：保持你原来功能不变
                viewModel.addDiary(
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tags,
                    coverUri = coverUriStr,
                    audioUri = audioUriStr,
                    videoUri = videoUriStr
                )
            }

            parentFragmentManager.popBackStack()
        }
    }

    private fun persistReadPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // 有的 Uri 不支持持久化权限，忽略即可
        }
    }
}
