package com.example.jian2.ui.diary.list

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.detail.DiaryDetailFragment
import com.example.jian2.ui.diary.write.WriteDiaryFragment
import com.example.jian2.ui.search.SearchFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class DiaryListFragment : Fragment(R.layout.fragment_diary_list) {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvDiary)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        val btnGoSearch = view.findViewById<MaterialButton>(R.id.btnGoSearch)

        val adapter = DiaryListAdapter { diary ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(diary.id))
                .addToBackStack("diary_detail")
                .commit()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // ✅ 进入就加载历史日记
        viewModel.loadDiaries()

        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WriteDiaryFragment.newInstanceCreate())
                .addToBackStack("write_create")
                .commit()
        }

        btnGoSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment())
                .addToBackStack("search")
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.diaries.collect { list ->
                adapter.submitList(list)
                val isEmpty = list.isEmpty()
                emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 返回列表时刷新（编辑/置顶后更直观）
        viewModel.loadDiaries()
    }
}
