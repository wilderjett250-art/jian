package com.example.jian2.ui.diary
import kotlinx.coroutines.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).diaryDao()

    // =========================
    // 列表（DiaryListFragment）
    // =========================
    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries

    fun loadDiaries() {
        viewModelScope.launch {
            _diaries.value = dao.getAll()
        }
    }

    fun getDiaryByIdFromCache(id: Long): DiaryEntity? =
        _diaries.value.firstOrNull { it.id == id }

    // =========================
    // 详情（DiaryDetailFragment）
    // =========================
    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing

    private var editingJob: Job? = null

    fun loadDiaryById(id: Long) {
        editingJob?.cancel()
        editingJob = viewModelScope.launch {
            dao.observeById(id).collect { e ->
                _editing.value = e
            }
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch {
            dao.setPinned(id, pinned)
            loadDiaries()
            // 详情如果正看这条，顺便刷新一次
            if (_editing.value?.id == id) {
                _editing.value = dao.getById(id)
            }
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            if (_editing.value?.id == id) _editing.value = null
            loadDiaries()
        }
    }

    fun exportDiaryAsText(id: Long, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val e = dao.getById(id)
            if (e == null) {
                onResult("(日记不存在)")
                return@launch
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val text = buildString {
                appendLine("标题：${e.title.ifBlank { "(无标题)" }}")
                appendLine("时间：${sdf.format(Date(e.createdAt))}")
                appendLine("心情：${e.mood}/5")
                if (e.tagsText.isNotBlank()) appendLine("标签：${e.tagsText}")
                appendLine()
                appendLine(e.content)
                if (!e.coverUri.isNullOrBlank()) appendLine("\n[封面] ${e.coverUri}")
                if (!e.audioUri.isNullOrBlank()) appendLine("[音频] ${e.audioUri}")
                if (!e.videoUri.isNullOrBlank()) appendLine("[视频] ${e.videoUri}")
            }
            onResult(text)
        }
    }

    // =========================
    // 写日记（WriteDiaryFragment）
    // =========================
    fun addDiary(
        title: String,
        content: String,
        mood: Int,
        tagsText: String? = null,
        coverUri: String? = null,
        audioUri: String? = null,
        videoUri: String? = null
    ) {
        viewModelScope.launch {
            dao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood,
                    // ✅ 关键：避免 String? -> String 报错
                    tagsText = tagsText ?: "",
                    coverUri = coverUri,
                    audioUri = audioUri,
                    videoUri = videoUri
                )
            )
            loadDiaries()
        }
    }

    fun updateDiary(
        id: Long,
        title: String,
        content: String,
        mood: Int,
        tagsText: String? = null,
        coverUri: String? = null,
        audioUri: String? = null,
        videoUri: String? = null
    ) {
        viewModelScope.launch {
            val old = dao.getById(id) ?: return@launch
            val updated = old.copy(
                title = title,
                content = content,
                mood = mood,
                tagsText = tagsText ?: old.tagsText,
                coverUri = coverUri,
                audioUri = audioUri,
                videoUri = videoUri
            )
            dao.update(updated)
            loadDiaries()
            if (_editing.value?.id == id) _editing.value = updated
        }
    }

    // =========================
    // Profile（ProfileFragment）
    // =========================
    data class ProfileStats(
        val totalCount: Int = 0,
        val monthCount: Int = 0,
        val monthAvgMood: String = "-",
        val monthDays: Int = 0,
        val streakDays: Int = 0
    )

    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats

    fun loadProfileStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()

            // 本月开始 00:00:00
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            // 下月开始
            cal.add(Calendar.MONTH, 1)
            val nextMonthStart = cal.timeInMillis

            val total = dao.countAll()
            val monthCount = dao.countByRange(monthStart, nextMonthStart)
            val monthAvg = dao.avgMoodByRange(monthStart, nextMonthStart)
            val monthDays = dao.distinctDaysCountByRange(monthStart, nextMonthStart)

            // 连续打卡：用最近 N 天的 yyyy-MM-dd（倒序）计算
            val streak = calcStreakDays(dao.getDistinctDaysDesc(60))

            _profileStats.value = ProfileStats(
                totalCount = total,
                monthCount = monthCount,
                monthAvgMood = monthAvg?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "-",
                monthDays = monthDays,
                streakDays = streak
            )
        }
    }

    private fun calcStreakDays(daysDesc: List<String>): Int {
        if (daysDesc.isEmpty()) return 0
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        fun toDay0(millis: Long): Long {
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        val today0 = toDay0(System.currentTimeMillis())
        val first = fmt.parse(daysDesc[0]) ?: return 0
        var expected = toDay0(first.time)

        // 允许第一条是今天或昨天
        if (expected != today0) {
            val yesterday0 = today0 - 24L * 60 * 60 * 1000
            if (expected != yesterday0) return 0
        }

        var streak = 0
        for (d in daysDesc) {
            val p = fmt.parse(d) ?: break
            val day0 = toDay0(p.time)
            if (day0 != expected) break
            streak++
            expected -= 24L * 60 * 60 * 1000
        }
        return streak
    }

    fun exportRecentAsText(limit: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val list = dao.getRecent(limit.coerceAtLeast(1))
            if (list.isEmpty()) {
                onResult("(暂无日记)")
                return@launch
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val text = buildString {
                appendLine("墨笺日记导出（最近${list.size}条）")
                appendLine("--------------------------------")
                list.forEachIndexed { idx, e ->
                    appendLine()
                    appendLine("#${idx + 1} ${e.title.ifBlank { "(无标题)" }}")
                    appendLine("时间：${sdf.format(Date(e.createdAt))}    心情：${e.mood}/5")
                    if (e.tagsText.isNotBlank()) appendLine("标签：${e.tagsText}")
                    appendLine(e.content)
                }
            }
            onResult(text)
        }
    }


    // ✅ 兼容 SearchFragment：支持心情区间 + 回调返回结果（不影响已有 searchAdvanced）
    fun searchAdvanced(
        query: String,
        tag: String?,
        moodMin: Int,
        moodMax: Int,
        onResult: (List<DiaryEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val all = dao.getAll()

            val q = query.trim()
            val t = tag?.trim().orEmpty()

            val lo = moodMin.coerceAtMost(moodMax)
            val hi = moodMax.coerceAtLeast(moodMin)

            val filtered = all.filter { d ->
                val hitQuery =
                    q.isBlank() || d.title.contains(q, ignoreCase = true) || d.content.contains(q, ignoreCase = true)

                val hitTag =
                    t.isBlank() || d.tagsText.contains(t, ignoreCase = true)

                val hitMoodRange =
                    d.mood in lo..hi

                hitQuery && hitTag && hitMoodRange
            }

            // 保持你 SearchFragment 的调用方式：直接回调 List<DiaryEntity>
            onResult(filtered)
        }
    }

    // =========================
    // Calendar（日历页面）
    // =========================
    private val _dayDiaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val dayDiaries: StateFlow<List<DiaryEntity>> = _dayDiaries

    fun loadDiariesForDay(dayMillis: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.timeInMillis = dayMillis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val end = cal.timeInMillis

            // 如果你 Dao 有 getByRange(start,end) 就用，没有就 fallback 用 getAll 过滤
            val list = try {
                dao.getByRange(start, end)
            } catch (_: Throwable) {
                dao.getAll().filter { it.createdAt in start until end }
            }

            _dayDiaries.value = list
        }
    }

    // =========================
    // Search（搜索页面）
    // =========================
    private val _searchResults = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntity>> = _searchResults

    fun searchAdvanced(
        query: String,
        tag: String? = null,
        mood: Int? = null,
        pinnedOnly: Boolean = false
    ) {
        viewModelScope.launch {
            val all = dao.getAll()

            val q = query.trim()
            val t = tag?.trim().orEmpty()

            val filtered = all.filter { d ->
                val hitQuery =
                    q.isBlank() || d.title.contains(q, ignoreCase = true) || d.content.contains(q, ignoreCase = true)

                val hitTag =
                    t.isBlank() || d.tagsText.contains(t, ignoreCase = true)

                val hitMood =
                    mood?.let { d.mood == it } ?: true

                val hitPinned =
                    if (pinnedOnly) d.isPinned else true

                hitQuery && hitTag && hitMood && hitPinned
            }

            _searchResults.value = filtered
        }
    }
}
