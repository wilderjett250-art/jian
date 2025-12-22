package com.example.jian2.ui.diary.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.write.WriteDiaryFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class DiaryDetailFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_diary_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEdit)
        val btnPin = view.findViewById<MaterialButton>(R.id.btnPin)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)

        val id = arguments?.getLong(ARG_ID, 0L) ?: 0L
        if (id == 0L) {
            Toast.makeText(requireContext(), "参数错误", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // ✅ 加载并监听详情
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDiaryById(id)
            viewModel.editing.collect { entity ->
                if (entity != null && entity.id == id) {
                    tvTitle.text = entity.title
                    tvContent.text = entity.content
                    btnPin.text = if (entity.isPinned) "取消置顶" else "置顶"
                }
            }
        }

        // ✅ 关键点 2：编辑按钮真正可用
        btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WriteDiaryFragment.newInstanceEdit(id))
                .addToBackStack("write_diary_edit")
                .commit()
        }

        // 置顶/取消置顶
        btnPin.setOnClickListener {
            val entity = viewModel.editing.value ?: return@setOnClickListener
            viewModel.setPinned(entity.id, !entity.isPinned)
            Toast.makeText(
                requireContext(),
                if (entity.isPinned) "已取消置顶" else "已置顶",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 删除
        btnDelete.setOnClickListener {
            viewModel.deleteDiary(id)
            Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_ID = "arg_id"

        fun newInstance(id: Long): DiaryDetailFragment {
            return DiaryDetailFragment().apply {
                arguments = Bundle().apply { putLong(ARG_ID, id) }
            }
        }
    }
}
