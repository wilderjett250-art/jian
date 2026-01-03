package com.example.jian2.ui.diary.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_media",
    indices = [Index("diaryId")]
)
data class DiaryMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val diaryId: Long,

    /**
     * 1=图片 2=语音 3=视频
     */
    val mediaType: Int,

    val uri: String
)
