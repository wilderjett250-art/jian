package com.example.jian2.ui.diary.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.jian2.R
import com.google.android.material.appbar.MaterialToolbar

class DiaryDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_diary_detail, container, false)

        val toolbar = root.findViewById<MaterialToolbar>(R.id.detailToolbar)
        val tvTitle = root.findViewById<TextView>(R.id.tvDetailTitle)
        val tvContent = root.findViewById<TextView>(R.id.tvDetailContent)
        val tvMood = root.findViewById<TextView>(R.id.tvDetailMood)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val content = requireArguments().getString(ARG_CONTENT).orEmpty()
        val mood = requireArguments().getInt(ARG_MOOD, 0)

        toolbar.title = "日记详情"
        tvTitle.text = title
        tvContent.text = content
        tvMood.text = "心情值：$mood"

        return root
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_MOOD = "arg_mood"

        fun newInstance(title: String, content: String, mood: Int): DiaryDetailFragment {
            return DiaryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_CONTENT, content)
                    putInt(ARG_MOOD, mood)
                }
            }
        }
    }
}
