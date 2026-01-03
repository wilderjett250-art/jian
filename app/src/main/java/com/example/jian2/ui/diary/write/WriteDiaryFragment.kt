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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class WriteDiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    companion object {
        private const val ARG_EDIT_ID = "arg_edit_id"

        fun newInstanceEdit(id: Long): WriteDiaryFragment {
            return WriteDiaryFragment().apply {
                arguments = Bundle().apply { putLong(ARG_EDIT_ID, id) }
            }
        }
    }

    private var editId: Long = 0L

    private var coverUriStr: String? = null
    private var audioUriStr: String? = null
    private var videoUriStr: String? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editId = arguments?.getLong(ARG_EDIT_ID, 0L) ?: 0L
        if (editId != 0L) {
            viewModel.loadDiaryById(editId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_write_diary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        // 编辑模式：回填
        if (editId != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.editing.collect { e ->
                    if (e == null || e.id != editId) return@collect
                    etTitle.setText(e.title)
                    etContent.setText(e.content)
                    etTags.setText(e.tagsText)
                    moodBar.progress = e.mood

                    coverUriStr = e.coverUri
                    audioUriStr = e.audioUri
                    videoUriStr = e.videoUri

                    e.coverUri?.let { uriStr ->
                        view.findViewById<ImageView>(R.id.ivCover)?.apply {
                            visibility = View.VISIBLE
                            setImageURI(Uri.parse(uriStr))
                        }
                    }
                    view.findViewById<TextView>(R.id.tvAudioHint)?.text =
                        if (e.audioUri.isNullOrBlank()) "未选择音频" else "已选择音频：${Uri.parse(e.audioUri).lastPathSegment ?: e.audioUri}"
                    view.findViewById<TextView>(R.id.tvVideoHint)?.text =
                        if (e.videoUri.isNullOrBlank()) "未选择视频" else "已选择视频：${Uri.parse(e.videoUri).lastPathSegment ?: e.videoUri}"
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val tags = etTags.text.toString().trim()

            if (title.isBlank() && content.isBlank()) {
                Toast.makeText(requireContext(), "标题和内容不能都为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editId == 0L) {
                viewModel.addDiary(
                    title = title,
                    content = content,
                    mood = moodBar.progress,
                    tagsText = tags.ifBlank { null },
                    coverUri = coverUriStr,
                    audioUri = audioUriStr,
                    videoUri = videoUriStr
                )
            } else {
                viewModel.updateDiary(
                    id = editId,
                    title = title,
                    content = content,
                    mood = moodBar.progress,
                    tagsText = tags.ifBlank { null },
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
        }
    }
}
