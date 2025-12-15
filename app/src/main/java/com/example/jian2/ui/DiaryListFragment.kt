package com.example.jian2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryListAdapter
import com.example.jian2.ui.diary.DiaryUiModel
import com.example.jian2.ui.diary.detail.DiaryDetailFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DiaryListFragment : Fragment() {

    private lateinit var rvDiary: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val adapter by lazy {
        DiaryListAdapter { item ->
            openDetail(item)
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
            // commit7 你说做“可点击详情”，这里先不做新增页
            // 下一次再实现写日记页
        }

        // ✅ 假数据：用你真实字段名 contentPreview / dateText
        val mock = listOf(
            DiaryUiModel(
                id = 1,
                title = "第一篇日记",
                contentPreview = "今天把项目跑通了，开始做日记本应用。",
                dateText = "2025-12-16",
                mood = 4,
                isPinned = true
            ),
            DiaryUiModel(
                id = 2,
                title = "第二篇日记",
                contentPreview = "完成了列表骨架，下一步写新增页面。",
                dateText = "2025-12-16",
                mood = 3,
                isPinned = false
            ),
            DiaryUiModel(
                id = 3,
                title = "第三篇日记",
                contentPreview = "准备接入数据库。",
                dateText = "2025-12-16",
                mood = 5,
                isPinned = false
            )
        )

        render(mock)
    }

    private fun render(list: List<DiaryUiModel>) {
        adapter.submitList(list)
        val isEmpty = list.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvDiary.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openDetail(item: DiaryUiModel) {
        val fragment = DiaryDetailFragment.newInstance(
            title = item.title,
            content = item.contentPreview, // ✅ 这里用 contentPreview 代替 content
            mood = item.mood
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
