package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).diaryDao()

    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries

    fun loadDiaries() {
        viewModelScope.launch {
            _diaries.value = dao.getAll()
        }
    }

    fun addDiary(title: String, content: String, mood: Int) {
        viewModelScope.launch {
            dao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood
                )
            )
            loadDiaries()
        }
    }
}
