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
import com.example.jian2.ui.diary.data.DiaryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryListAdapter(
    private val onClick: (DiaryEntity) -> Unit
) : ListAdapter<DiaryEntity, DiaryListAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DiaryEntity>() {
            override fun areItemsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity) = oldItem == newItem
        }

        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        private fun moodEmoji(m: Int): String = when {
            m >= 85 -> "😄"
            m >= 70 -> "🙂"
            m >= 55 -> "😐"
            m >= 40 -> "😕"
            else -> "😭"
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvMood: TextView = itemView.findViewById(R.id.tvMood)
        private val ivPin: ImageView = itemView.findViewById(R.id.ivPin)

        fun bind(item: DiaryEntity) {
            tvTitle.text = item.title.ifBlank { "(无标题)" }
            tvPreview.text = item.content.take(40).let { if (item.content.length > 40) "$it…" else it }
            tvDate.text = sdf.format(Date(item.createdAt))

            tvMood.text = "心情：${moodEmoji(item.mood)} ${item.mood}"
            ivPin.visibility = if (item.isPinned) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
