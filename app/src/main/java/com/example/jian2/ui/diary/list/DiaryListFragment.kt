package com.example.jian2.ui.diary.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DiaryListFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    private lateinit var rvDiary: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val adapter by lazy {
        DiaryListAdapter { entity ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(entity.id))
                .addToBackStack("diary_detail")
                .commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_diary_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDiary = view.findViewById(R.id.rvDiary)
        emptyState = view.findViewById(R.id.emptyState)
        fabAdd = view.findViewById(R.id.fabAdd)

        rvDiary.layoutManager = LinearLayoutManager(requireContext())
        rvDiary.adapter = adapter

        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WriteDiaryFragment())
                .addToBackStack("write")
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.diaries.collect { list ->
                adapter.submitList(list)
                val isEmpty = list.isEmpty()
                emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvDiary.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        viewModel.loadDiaries()
    }
}
