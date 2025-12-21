package com.example.jian2.ui.diary.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.jian2.R
import com.example.jian2.ui.diary.list.DiaryUiModel

class DiaryDetailFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvMood: TextView
    private lateinit var tvContent: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_diary_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvDetailTitle)
        tvDate = view.findViewById(R.id.tvDetailDate)
        tvMood = view.findViewById(R.id.tvDetailMood)
        tvContent = view.findViewById(R.id.tvDetailContent)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val preview = requireArguments().getString(ARG_PREVIEW).orEmpty()
        val dateText = requireArguments().getString(ARG_DATE).orEmpty()
        val mood = requireArguments().getInt(ARG_MOOD, 0)

        tvTitle.text = title
        tvDate.text = dateText
        tvMood.text = moodText(mood)
        tvContent.text = preview
    }

    private fun moodText(mood: Int): String {
        return when (mood) {
            1 -> "很差"
            2 -> "一般"
            3 -> "还行"
            4 -> "不错"
            5 -> "超棒"
            else -> "未知"
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_PREVIEW = "arg_preview"
        private const val ARG_DATE = "arg_date"
        private const val ARG_MOOD = "arg_mood"

        fun newInstance(item: DiaryUiModel): DiaryDetailFragment {
            val f = DiaryDetailFragment()
            f.arguments = Bundle().apply {
                putString(ARG_TITLE, item.title)
                putString(ARG_PREVIEW, item.contentPreview)
                putString(ARG_DATE, item.dateText)
                putInt(ARG_MOOD, item.mood)
            }
            return f
        }
    }
}
