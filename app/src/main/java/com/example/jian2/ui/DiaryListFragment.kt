package com.example.jian2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryListAdapter
import com.example.jian2.ui.diary.DiaryUiModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DiaryListFragment : Fragment() {

    private lateinit var rvDiary: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val adapter by lazy {
        DiaryListAdapter { item ->
            Toast.makeText(requireContext(), "点了：${item.title}", Toast.LENGTH_SHORT).show()
            // 下一次 commit 再做：跳转到详情页
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_diary_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDiary = view.findViewById(R.id.rvDiary)
        emptyState = view.findViewById(R.id.emptyState)
        fabAdd = view.findViewById(R.id.fabAdd)

        rvDiary.layoutManager = LinearLayoutManager(requireContext())
        rvDiary.adapter = adapter

        fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "下一步：进入写日记页（下次提交实现）", Toast.LENGTH_SHORT).show()
        }

        // 先用假数据占位（后面接 Room）
        val mock = listOf(
            DiaryUiModel(1, "第一篇日记", "今天把项目跑通了，开始做日记本应用。", "2025-12-16", mood = 4, isPinned = true),
            DiaryUiModel(2, "第二篇日记", "完成了列表骨架，下一步写新增页面。", "2025-12-16", mood = 3),
            DiaryUiModel(3, "第三篇日记", "准备接入数据库。", "2025-12-16", mood = 5)
        )

        render(mock)
    }

    private fun render(list: List<DiaryUiModel>) {
        adapter.submitList(list)
        val isEmpty = list.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvDiary.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
