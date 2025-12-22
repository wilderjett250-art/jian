package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).diaryDao()

    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries

    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing

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

    fun clearEditing() {
        _editing.value = null
    }

    fun addDiary(title: String, content: String, mood: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood
                )
            )
            _diaries.value = dao.getAll()
        }
    }

    /**
     * ✅ 兼容你现在 Fragment 调用的 4 参数版本：updateDiary(id, title, content, mood)
     * 自动保留原来的 createdAt / isPinned
     */
    fun updateDiary(id: Long, title: String, content: String, mood: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val old = dao.getById(id) ?: return@launch
            dao.update(
                old.copy(
                    title = title,
                    content = content,
                    mood = mood,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _diaries.value = dao.getAll()
            _editing.value = dao.getById(id)
        }
    }

    /**
     * ✅ 如果你项目里还有地方用旧签名，也不会炸（可保留）
     */
    fun updateDiary(id: Long, title: String, content: String, mood: Int, isPinned: Boolean, createdAt: Long) {
        viewModelScope.launch(Dispatchers.IO) {
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
            _diaries.value = dao.getAll()
            _editing.value = dao.getById(id)
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
            _diaries.value = dao.getAll()
            if (_editing.value?.id == id) _editing.value = null
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setPinned(id, pinned) // DAO 里 updatedAt 有默认值
            _diaries.value = dao.getAll()
            if (_editing.value?.id == id) {
                _editing.value = dao.getById(id)
            }
        }
    }

    fun search(keyword: String, onResult: (List<DiaryEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val kw = "%${keyword.trim()}%"
            val result = dao.search(kw)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}
