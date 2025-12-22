package com.example.jian2.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.detail.DiaryDetailFragment
import com.example.jian2.ui.diary.list.DiaryListAdapter
import com.google.android.material.button.MaterialButton

class SearchFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etKeyword = view.findViewById<EditText>(R.id.etKeyword)
        val btnSearch = view.findViewById<MaterialButton>(R.id.btnSearch)
        val rv = view.findViewById<RecyclerView>(R.id.rvResult)

        val adapter = DiaryListAdapter { item ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(item.id))
                .addToBackStack("detail")
                .commit()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnSearch.setOnClickListener {
            val kw = etKeyword.text.toString()
            viewModel.search(kw) { result ->
                // 这里 adapter 如果吃的是 DiaryEntity，就直接 submitList(result)
                adapter.submitList(result)
            }
        }
    }
}
