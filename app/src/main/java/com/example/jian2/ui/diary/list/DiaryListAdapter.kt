package com.example.jian2.ui.diary.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvTitle.text = item.title
        holder.tvPreview.text = item.content.take(40)
        holder.tvDate.text = formatDate(item.createdAt)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    private fun formatDate(ts: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(ts))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DiaryEntity>() {
            override fun areItemsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DiaryEntity, newItem: DiaryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
