package com.example.jian2.ui.diary.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.write.WriteDiaryFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryDetailFragment : Fragment(R.layout.fragment_diary_detail) {

    private val viewModel: DiaryViewModel by activityViewModels()

    companion object {
        private const val ARG_ID = "arg_id"

        fun newInstance(id: Long): DiaryDetailFragment {
            return DiaryDetailFragment().apply {
                arguments = Bundle().apply { putLong(ARG_ID, id) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMeta = view.findViewById<TextView>(R.id.tvMeta)
        val tvTags = view.findViewById<TextView>(R.id.tvTags)
        val ivCover = view.findViewById<ImageView>(R.id.ivCover)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)

        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEdit)
        val btnPin = view.findViewById<MaterialButton>(R.id.btnPin)
        val btnShare = view.findViewById<MaterialButton>(R.id.btnShare)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)

        val id = arguments?.getLong(ARG_ID, 0L) ?: 0L
        if (id == 0L) {
            toast("日记ID无效")
            parentFragmentManager.popBackStack()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        viewModel.loadDiaryById(id)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editing.collect { e ->
                if (e == null || e.id != id) return@collect

                tvTitle.text = e.title.ifBlank { "(无标题)" }
                tvContent.text = e.content
                tvMeta.text = "时间：${sdf.format(Date(e.createdAt))}    心情：${e.mood}/5"

                if (e.tagsText.isBlank()) {
                    tvTags.visibility = View.GONE
                } else {
                    tvTags.visibility = View.VISIBLE
                    tvTags.text = "标签：${e.tagsText}"
                }

                if (e.coverUri.isNullOrBlank()) {
                    ivCover.visibility = View.GONE
                } else {
                    ivCover.visibility = View.VISIBLE
                    ivCover.load(e.coverUri) { crossfade(true) }
                }

                btnPin.text = if (e.isPinned) "取消置顶" else "置顶"
            }
        }

        btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WriteDiaryFragment.newInstanceEdit(id))
                .addToBackStack("write_edit")
                .commit()
        }

        btnPin.setOnClickListener {
            val e = viewModel.editing.value ?: return@setOnClickListener
            viewModel.setPinned(e.id, !e.isPinned)
            toast(if (!e.isPinned) "已置顶" else "已取消置顶")
        }

        btnShare.setOnClickListener {
            viewModel.exportDiaryAsText(id) { text ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "墨笺日记分享")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(intent, "分享"))
            }
        }

        btnDelete.setOnClickListener {
            viewModel.deleteDiary(id)
            toast("已删除")
            parentFragmentManager.popBackStack()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
