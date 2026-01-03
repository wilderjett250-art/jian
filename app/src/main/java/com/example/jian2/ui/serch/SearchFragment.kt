package com.example.jian2.ui.search
import com.example.jian2.ui.diary.data.DiaryEntity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.detail.DiaryDetailFragment
import com.example.jian2.ui.diary.list.DiaryListAdapter
import com.google.android.material.button.MaterialButton

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etKeyword = view.findViewById<EditText>(R.id.etKeyword)
        val etTag = view.findViewById<EditText>(R.id.etTag)

        val tvMin = view.findViewById<TextView>(R.id.tvMoodMin)
        val tvMax = view.findViewById<TextView>(R.id.tvMoodMax)
        val seekMin = view.findViewById<SeekBar>(R.id.seekMoodMin)
        val seekMax = view.findViewById<SeekBar>(R.id.seekMoodMax)

        val btnSearch = view.findViewById<MaterialButton>(R.id.btnSearch)
        val rv = view.findViewById<RecyclerView>(R.id.rvResult)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        val adapter = DiaryListAdapter { entity ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(entity.id))
                .addToBackStack("diary_detail")
                .commit()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun refreshMoodText() {
            tvMin.text = "心情最小值：${seekMin.progress}"
            tvMax.text = "心情最大值：${seekMax.progress}"
        }
        refreshMoodText()

        seekMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > seekMax.progress) seekMax.progress = progress
                refreshMoodText()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < seekMin.progress) seekMin.progress = progress
                refreshMoodText()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSearch.setOnClickListener {
            val kw = etKeyword.text.toString()
            val tag = etTag.text.toString()
            val min = seekMin.progress
            val max = seekMax.progress
            viewModel.searchAdvanced(kw, tag, min, max) { result: List<DiaryEntity> ->

                adapter.submitList(result)
                val empty = result.isEmpty()
                tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                rv.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }
    }
}
