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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).diaryDao()

    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries

    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing

    private val _dayDiaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val dayDiaries: StateFlow<List<DiaryEntity>> = _dayDiaries

    data class ProfileStats(
        val totalCount: Int = 0,
        val monthCount: Int = 0,
        val monthAvgMood: Double = 0.0,
        val monthDays: Int = 0,
        val streakDays: Int = 0
    )

    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats

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

    fun addDiary(title: String, content: String, mood: Int, tagsText: String, coverUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tagsText,
                    coverUri = coverUri
                )
            )
            loadDiaries()
            loadProfileStats()
        }
    }

    fun updateDiary(
        id: Long,
        title: String,
        content: String,
        mood: Int,
        tagsText: String,
        coverUri: String?,
        isPinned: Boolean,
        createdAt: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(
                DiaryEntity(
                    id = id,
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tagsText,
                    coverUri = coverUri,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis(),
                    isPinned = isPinned
                )
            )
            loadDiaries()
            loadProfileStats()
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
            if (_editing.value?.id == id) _editing.value = null
            loadDiaries()
            loadProfileStats()
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setPinned(id, pinned)
            // 详情页如果正在看这条，也更新一下
            val cur = _editing.value
            if (cur != null && cur.id == id) {
                _editing.value = cur.copy(isPinned = pinned, updatedAt = System.currentTimeMillis())
            }
            loadDiaries()
        }
    }

    fun searchAdvanced(
        keyword: String,
        tag: String,
        moodMin: Int,
        moodMax: Int,
        onResult: (List<DiaryEntity>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val kw = "%${keyword.trim()}%"
            val safeTag = tag.trim()
            val min = moodMin.coerceIn(0, 5)
            val max = moodMax.coerceIn(0, 5).coerceAtLeast(min)
            val result = dao.searchAdvanced(kw, safeTag, min, max)
            withContext(Dispatchers.Main) { onResult(result) }
        }
    }

    fun loadDiariesForDay(dayStartMillis: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val oneDay = 24L * 60L * 60L * 1000L
            _dayDiaries.value = dao.getByDateRange(dayStartMillis, dayStartMillis + oneDay)
        }
    }

    fun loadProfileStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val monthStart = startOfMonthMillis(now)
            val nextMonthStart = startOfNextMonthMillis(now)

            val total = dao.countAll()
            val monthCount = dao.countByRange(monthStart, nextMonthStart)
            val avg = (dao.avgMoodByRange(monthStart, nextMonthStart) ?: 0.0)
            val monthDays = dao.distinctDaysCountByRange(monthStart, nextMonthStart)
            val streak = calcStreakDays(dao.getDistinctDaysDesc(60))

            _profileStats.value = ProfileStats(
                totalCount = total,
                monthCount = monthCount,
                monthAvgMood = (avg * 10.0).roundToInt() / 10.0,
                monthDays = monthDays,
                streakDays = streak
            )
        }
    }

    fun exportRecentAsText(limit: Int, onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dao.getRecent(limit)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val text = buildString {
                append("《墨笺》日记导出（最近").append(limit).append("条）\n")
                append("导出时间：").append(Date()).append("\n\n")
                if (list.isEmpty()) {
                    append("（暂无日记）\n")
                } else {
                    list.forEachIndexed { i, e ->
                        append("【").append(i + 1).append("】").append(e.title.ifBlank { "(无标题)" }).append("\n")
                        append("时间：").append(sdf.format(Date(e.createdAt)))
                            .append("  心情：").append(e.mood).append("/5")
                            .append("  置顶：").append(if (e.isPinned) "是" else "否").append("\n")
                        if (e.tagsText.isNotBlank()) append("标签：").append(e.tagsText).append("\n")
                        append(e.content).append("\n")
                        append("--------------------------------------------------\n\n")
                    }
                }
            }
            withContext(Dispatchers.Main) { onReady(text) }
        }
    }

    fun exportDiaryAsText(id: Long, onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val e = dao.getById(id)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val text = if (e == null) {
                "（日记不存在）"
            } else buildString {
                append("《墨笺》日记分享\n\n")
                append("标题：").append(e.title.ifBlank { "(无标题)" }).append("\n")
                append("时间：").append(sdf.format(Date(e.createdAt))).append("\n")
                append("心情：").append(e.mood).append("/5").append("\n")
                if (e.tagsText.isNotBlank()) append("标签：").append(e.tagsText).append("\n")
                append("\n").append(e.content).append("\n")
            }
            withContext(Dispatchers.Main) { onReady(text) }
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

    private fun calcStreakDays(distinctDaysDesc: List<String>): Int {
        // distinctDaysDesc: ["2025-12-24","2025-12-23"...]
        if (distinctDaysDesc.isEmpty()) return 0
        val set = distinctDaysDesc.toHashSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var streak = 0
        while (true) {
            val key = sdf.format(cal.time)
            if (set.contains(key)) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else break
        }
        return streak
    }
}
