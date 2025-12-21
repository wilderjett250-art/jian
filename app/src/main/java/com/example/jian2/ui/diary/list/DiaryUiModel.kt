package com.example.jian2.ui.diary.list

data class DiaryUiModel(
    val id: Long,
    val title: String,
    val contentPreview: String,
    val dateText: String,
    val mood: Int,
    val isPinned: Boolean = false
)
