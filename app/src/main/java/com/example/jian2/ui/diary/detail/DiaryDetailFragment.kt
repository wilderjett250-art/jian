package com.example.jian2.ui.diary.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import kotlinx.coroutines.launch

class DiaryDetailFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_diary_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)

        val id = requireArguments().getLong(ARG_ID)

        viewLifecycleOwner.lifecycleScope.launch {
            val diary = viewModel.getDiaryById(id)
            tvTitle.text = diary?.title ?: "未找到"
            tvContent.text = diary?.content ?: ""
        }
    }

    companion object {
        private const val ARG_ID = "diary_id"

        fun newInstance(id: Long): DiaryDetailFragment {
            return DiaryDetailFragment().apply {
                arguments = Bundle().apply { putLong(ARG_ID, id) }
            }
        }
    }
}
