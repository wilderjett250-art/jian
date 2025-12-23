package com.example.jian2.ui.diary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val mood: Int,                 // 0~5
    val tagsText: String = "",     // 标签：逗号分隔，例如 "学习,探店"
    val coverUri: String? = null,  // 封面图：单张
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
