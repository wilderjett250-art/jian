package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).diaryDao()

    val diaries: Flow<List<DiaryEntity>> = dao.observeAll()

    fun addDiary(title: String, content: String, mood: Int) {
        viewModelScope.launch {
            dao.insert(
                DiaryEntity(
                    title = title.trim(),
                    content = content.trim(),
                    mood = mood
                )
            )
        }
    }

    suspend fun getDiaryById(id: Long): DiaryEntity? {
        return dao.getById(id)
    }
}
