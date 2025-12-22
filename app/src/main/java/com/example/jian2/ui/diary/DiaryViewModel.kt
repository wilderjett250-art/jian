package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).diaryDao()

    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries.asStateFlow()

    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing.asStateFlow()

    fun loadDiaries() {
        viewModelScope.launch(Dispatchers.IO) {
            _diaries.value = dao.getAll()
        }
    }

    fun loadDiaryById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _editing.value = dao.getById(id)
        }
    }

    fun addDiary(title: String, content: String, mood: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            dao.insert(
                DiaryEntity(
                    id = 0L,
                    title = title,
                    content = content,
                    mood = mood,
                    createdAt = now,
                    updatedAt = now,
                    isPinned = false
                )
            )
            loadDiaries()
        }
    }

    fun updateDiary(id: Long, title: String, content: String, mood: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val old = dao.getById(id) ?: return@launch
            val now = System.currentTimeMillis()
            dao.update(
                old.copy(
                    title = title,
                    content = content,
                    mood = mood,
                    updatedAt = now
                )
            )
            loadDiaries()
            _editing.value = dao.getById(id)
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
            loadDiaries()
            if (_editing.value?.id == id) _editing.value = null
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch {
            dao.setPinned(id, pinned)
            loadDiaries()
            // ✅ 如果当前正在详情页查看这一条，顺带刷新 editing
            if (_editing.value?.id == id) {
                _editing.value = dao.getById(id)
            }
        }
    }


    fun search(keyword: String, onResult: (List<DiaryEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = dao.search(keyword)
            viewModelScope.launch(Dispatchers.Main) { onResult(result) }
        }
    }
}
