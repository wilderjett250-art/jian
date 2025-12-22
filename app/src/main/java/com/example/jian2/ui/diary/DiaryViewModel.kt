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

    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing

    // ✅ 新增：日历页“当天日记列表”
    private val _dayDiaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val dayDiaries: StateFlow<List<DiaryEntity>> = _dayDiaries

    fun loadDiaries() {
        viewModelScope.launch {
            _diaries.value = dao.getAll()
        }
    }

    fun loadDiaryById(id: Long) {
        viewModelScope.launch {
            _editing.value = dao.getById(id)
        }
    }

    fun clearEditing() {
        _editing.value = null
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

    fun updateDiary(id: Long, title: String, content: String, mood: Int, isPinned: Boolean, createdAt: Long) {
        viewModelScope.launch {
            dao.update(
                DiaryEntity(
                    id = id,
                    title = title,
                    content = content,
                    mood = mood,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis(),
                    isPinned = isPinned
                )
            )
            loadDiaries()
        }
    }
    // ✅ 兼容旧调用：只传 4 个参数也能更新（自动从数据库取 isPinned / createdAt）
    fun updateDiary(id: Long, title: String, content: String, mood: Int) {
        viewModelScope.launch {
            val old = dao.getById(id) ?: return@launch
            updateDiary(
                id = old.id,
                title = title,
                content = content,
                mood = mood,
                isPinned = old.isPinned,
                createdAt = old.createdAt
            )
        }
    }


    fun deleteDiary(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            loadDiaries()
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch {
            dao.setPinned(id, pinned)
            loadDiaries()
        }
    }

    fun search(keyword: String, onResult: (List<DiaryEntity>) -> Unit) {
        viewModelScope.launch {
            val kw = "%${keyword.trim()}%"
            onResult(dao.search(kw))
        }
    }

    // ✅ 新增：给日历页用——加载某一天的日记（dayStartMillis=当天 00:00）
    fun loadDiariesForDay(dayStartMillis: Long) {
        viewModelScope.launch {
            val oneDay = 24L * 60L * 60L * 1000L
            _dayDiaries.value = dao.getByDateRange(dayStartMillis, dayStartMillis + oneDay)
        }
    }
}
