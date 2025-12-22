package com.example.jian2.ui.diary.list

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyState = view.findViewById<TextView>(R.id.emptyState)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        val btnGoSearch = view.findViewById<MaterialButton>(R.id.btnGoSearch)

        val adapter = DiaryListAdapter { diary ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(diary.id))
                .addToBackStack("diary_detail")
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // ✅ 进入页面就加载历史日记（你之前缺的关键点）
        viewModel.loadDiaries()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.diaries.collect { list ->
                    adapter.submitList(list)
                    emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WriteDiaryFragment.newInstanceCreate())
                .addToBackStack("write_create")
                .commit()
        }

        // ✅ SearchFragment 入口
        btnGoSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment())
                .addToBackStack("search")
                .commit()
        }
    }
}
