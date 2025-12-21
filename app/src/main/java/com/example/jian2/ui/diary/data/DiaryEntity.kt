package com.example.jian2.ui.diary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: Int,
    val createTime: Long = System.currentTimeMillis()
)
