package com.example.jian2.ui.diary.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R

class DiaryListAdapter(
    private val onClick: (DiaryUiModel) -> Unit
) : ListAdapter<DiaryUiModel, DiaryListAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<DiaryUiModel>() {
        override fun areItemsTheSame(oldItem: DiaryUiModel, newItem: DiaryUiModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DiaryUiModel, newItem: DiaryUiModel) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View, private val onClick: (DiaryUiModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvMood: TextView = itemView.findViewById(R.id.tvMood)
        private val ivPin: ImageView = itemView.findViewById(R.id.ivPin)

        private var current: DiaryUiModel? = null

        init {
            itemView.setOnClickListener {
                current?.let(onClick)
            }
        }

        fun bind(item: DiaryUiModel) {
            current = item

            tvTitle.text = item.title
            tvPreview.text = item.contentPreview
            tvDate.text = item.dateText
            tvMood.text = moodText(item.mood)

            ivPin.visibility = if (item.isPinned) View.VISIBLE else View.GONE
        }

        private fun moodText(mood: Int): String {
            return when (mood) {
                1 -> "心情：很差"
                2 -> "心情：一般"
                3 -> "心情：还行"
                4 -> "心情：不错"
                5 -> "心情：超棒"
                else -> "心情：未知"
            }
        }
    }
}
