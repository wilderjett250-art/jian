package com.example.jian2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryListAdapter
import com.example.jian2.ui.diary.DiaryUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryListFragment : Fragment() {

    private lateinit var adapter: DiaryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_diary_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvDiary = view.findViewById<RecyclerView>(R.id.rvDiary)

        adapter = DiaryListAdapter { item ->
            Toast.makeText(requireContext(), "点击：${item.title}", Toast.LENGTH_SHORT).show()
            // 下一次 commit 我们就做：跳转到详情页
        }

        rvDiary.adapter = adapter

        adapter.submitList(mockData())
    }

    private fun mockData(): List<DiaryUiModel> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        return listOf(
            DiaryUiModel(
                id = 1,
                title = "今天开始写笺日记本",
                contentPreview = "把今天的心情、图片、标签都记录下来。先把列表跑通！",
                dateText = today,
                moodEmoji = "🙂",
                tagsText = "#学习  #计划"
            ),
            DiaryUiModel(
                id = 2,
                title = "第二篇：我想坚持 30 天",
                contentPreview = "每天写一点点也行。明天再加：详情页 + 新增页。",
                dateText = today,
                moodEmoji = "😊",
                tagsText = "#习惯  #自律"
            ),
            DiaryUiModel(
                id = 3,
                title = "我想坚持一百天",
                contentPreview = "启动页、底部导航、列表、详情、写日记、数据库、搜索、日历、统计。",
                dateText = today,
                moodEmoji = "😎",
                tagsText = "#进度"
            )
        )
    }
}
