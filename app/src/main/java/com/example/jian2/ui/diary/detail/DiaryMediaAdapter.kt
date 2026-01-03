package com.example.jian2.ui.diary.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.data.DiaryMediaEntity

class DiaryMediaAdapter(
    private val onClick: (DiaryMediaEntity) -> Unit
) : ListAdapter<DiaryMediaEntity, DiaryMediaAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_media, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View, val onClick: (DiaryMediaEntity) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val tvName = itemView.findViewById<TextView>(R.id.tvName)

        fun bind(item: DiaryMediaEntity) {
            val typeText = when (item.mediaType) {
                1 -> "图片"
                2 -> "语音"
                3 -> "视频"
                else -> "附件"
            }
            tvName.text = "$typeText：${item.uri.take(30)}..."
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DiaryMediaEntity>() {
            override fun areItemsTheSame(oldItem: DiaryMediaEntity, newItem: DiaryMediaEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DiaryMediaEntity, newItem: DiaryMediaEntity) =
                oldItem == newItem
        }
    }
}
