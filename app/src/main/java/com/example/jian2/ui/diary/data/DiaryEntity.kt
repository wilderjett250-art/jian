package com.example.jian2.ui.diary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val mood: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,   // ← 必须存在
    val coverUri: String? = null,
    val audioUri: String? = null,
    val videoUri: String? = null,
    val tagsText: String = ""
)
