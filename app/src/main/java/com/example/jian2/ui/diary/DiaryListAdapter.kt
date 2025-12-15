package com.example.jian2.ui.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R

class DiaryListAdapter(
    private val onClick: (DiaryUiModel) -> Unit
) : ListAdapter<DiaryUiModel, DiaryListAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<DiaryUiModel>() {
        override fun areItemsTheSame(oldItem: DiaryUiModel, newItem: DiaryUiModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DiaryUiModel, newItem: DiaryUiModel) = oldItem == newItem
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)

        fun bind(item: DiaryUiModel) {
            tvTitle.text = if (item.isPinned) "📌 ${item.title}" else item.title
            tvPreview.text = item.contentPreview
            tvMeta.text = "${moodEmoji(item.mood)}  ${item.dateText}"

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun moodEmoji(mood: Int): String {
        return when (mood) {
            1 -> "😞"
            2 -> "😐"
            3 -> "🙂"
            4 -> "😄"
            5 -> "🤩"
            else -> "🙂"
        }
    }
}
