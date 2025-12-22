package com.example.jian2.ui.diary.detail

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.write.WriteDiaryFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class DiaryDetailFragment : Fragment(R.layout.fragment_diary_detail) {

    private val viewModel: DiaryViewModel by activityViewModels()

    companion object {
        private const val ARG_ID = "ARG_ID"
        fun newInstance(id: Long) = DiaryDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_ID, id) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)

        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEdit)
        val btnPin = view.findViewById<MaterialButton>(R.id.btnPin)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)

        val id = arguments?.getLong(ARG_ID, 0L) ?: 0L
        if (id == 0L) {
            Toast.makeText(requireContext(), "日记ID无效", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        viewModel.loadDiaryById(id)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editing.collect { e ->
                    if (e == null || e.id != id) return@collect
                    tvTitle.text = e.title.ifBlank { "(无标题)" }
                    tvContent.text = e.content
                    btnPin.text = if (e.isPinned) "取消置顶" else "置顶"
                }
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
        }

        btnDelete.setOnClickListener {
            viewModel.deleteDiary(id)
            Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
