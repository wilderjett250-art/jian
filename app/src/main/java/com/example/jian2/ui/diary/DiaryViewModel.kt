package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar


class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    data class ProfileStats(
        val totalCount: Int = 0,
        val monthCount: Int = 0,
        val monthAvgMood: Double = 0.0
    )

    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats

    fun loadProfileStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val monthStart = startOfMonthMillis(now)
            val monthEnd = startOfNextMonthMillis(now)

            val total = dao.countAll()
            val monthCount = dao.countByRange(monthStart, monthEnd)
            val avg = dao.avgMoodByRange(monthStart, monthEnd) ?: 0.0

            _profileStats.value = ProfileStats(
                totalCount = total,
                monthCount = monthCount,
                monthAvgMood = avg
            )
        }
    }

    /**
     * ✅ 导出最近 N 条（纯文本），回调到主线程给 UI 用
     */
    fun exportRecentAsText(limit: Int, onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dao.getRecent(limit)

            val text = buildString {
                append("日记导出（最近").append(limit).append("条）\n")
                append("导出时间：").append(java.util.Date()).append("\n\n")
                if (list.isEmpty()) {
                    append("（暂无日记）\n")
                } else {
                    list.forEachIndexed { index, e ->
                        append("【").append(index + 1).append("】")
                            .append(e.title.ifBlank { "(无标题)" })
                            .append("\n")
                        append("时间：").append(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                            .format(java.util.Date(e.createdAt)))
                            .append("  心情：").append(e.mood)
                            .append("  置顶：").append(if (e.isPinned) "是" else "否")
                            .append("\n")
                        append(e.content).append("\n")
                        append("--------------------------------------------------\n\n")
                    }
                }
            }

            withContext(Dispatchers.Main) {
                onReady(text)
            }
        }
    }

    private fun startOfMonthMillis(anyMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = anyMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun startOfNextMonthMillis(anyMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = anyMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }


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
