package com.example.jian2.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.DiaryDao
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(private val dao: DiaryDao) : ViewModel() {

    val diaries: StateFlow<List<DiaryEntity>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDiary(title: String, content: String, mood: Int) {
        viewModelScope.launch {
            dao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood
                )
            )
        }
    }

    class Factory(private val dao: DiaryDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiaryViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
