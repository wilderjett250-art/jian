package com.example.jian2.ui.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jian2.ui.diary.data.AppDatabase
import com.example.jian2.ui.diary.data.DiaryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ProfileStats(
    val totalCount: Int = 0,
    val monthCount: Int = 0,
    val monthAvgMood: String = "0.00",
    val monthDays: Int = 0,
    val streakDays: Int = 0
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.get(application)
    private val diaryDao = db.diaryDao()
    private val mediaDao = db.diaryMediaDao()

    private val _diaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val diaries: StateFlow<List<DiaryEntity>> = _diaries

    /** 详情/编辑共用：当前查看的那一篇 */
    private val _editing = MutableStateFlow<DiaryEntity?>(null)
    val editing: StateFlow<DiaryEntity?> = _editing

    /** 日历：选中某天的列表 */
    private val _dayDiaries = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val dayDiaries: StateFlow<List<DiaryEntity>> = _dayDiaries

    /** 个人页统计 */
    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats: StateFlow<ProfileStats> = _profileStats

    /** 搜索结果：如果你想用“collect”方式也可以直接收这个 */
    private val _searchResults = MutableStateFlow<List<DiaryEntity>>(emptyList())
    val searchResults: StateFlow<List<DiaryEntity>> = _searchResults

    /** 列表：用 Flow 监听数据库（置顶/新增/删除能自动刷新） */
    fun loadDiaries() {
        viewModelScope.launch {
            diaryDao.observeAll().collect { list ->
                _diaries.value = list
            }
        }
    }

    /** 详情：监听某个 id（布局/详情页 collect editing 即可） */
    fun loadDiaryById(id: Long) {
        viewModelScope.launch {
            diaryDao.observeById(id).collect { e ->
                _editing.value = e
            }
        }
    }

    fun getDiaryByIdFromCache(id: Long): DiaryEntity? =
        _diaries.value.firstOrNull { it.id == id }

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
            diaryDao.insert(
                DiaryEntity(
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tagsText ?: "",
                    coverUri = coverUri,
                    audioUri = audioUri,
                    videoUri = videoUri
                )
            )
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
            val old = diaryDao.getById(id) ?: return@launch
            diaryDao.update(
                old.copy(
                    title = title,
                    content = content,
                    mood = mood,
                    tagsText = tagsText ?: "",
                    coverUri = coverUri,
                    audioUri = audioUri,
                    videoUri = videoUri,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch {
            diaryDao.deleteById(id)
        }
    }

    fun setPinned(id: Long, pinned: Boolean) {
        viewModelScope.launch {
            diaryDao.setPinned(id = id, pinned = pinned, updatedAt = System.currentTimeMillis())
        }
    }

    /** 日历：加载某天（dayStartMillis 是当天 00:00:00 的毫秒） */
    fun loadDiariesForDay(dayStartMillis: Long) {
        val dayEnd = dayStartMillis + 24L * 60 * 60 * 1000
        viewModelScope.launch {
            _dayDiaries.value = diaryDao.getByDateRange(dayStartMillis, dayEnd)
        }
    }

    /**
     * 高级搜索（给 SearchFragment 用：支持 moodMin/moodMax）
     * - query: 关键字（会自动做 LIKE %xx%）
     * - tag: 标签（空串表示不筛）
     */
    fun searchAdvanced(
        query: String,
        tag: String,
        moodMin: Int,
        moodMax: Int,
        onResult: (List<DiaryEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val kw = "%${query.trim()}%"
            val result = diaryDao.searchAdvanced(
                kw = kw,
                tag = tag.trim(),
                moodMin = moodMin,
                moodMax = moodMax
            )
            _searchResults.value = result
            onResult(result)
        }
    }

    /** 导出单篇为文本 */
    fun exportDiaryAsText(id: Long, onText: (String) -> Unit) {
        viewModelScope.launch {
            val e = diaryDao.getById(id) ?: run {
                onText("未找到该日记(id=$id)")
                return@launch
            }
            onText(formatDiary(e))
        }
    }

    /** 导出最近 N 篇（个人页分享用） */
    fun exportRecentAsText(limit: Int, onText: (String) -> Unit) {
        viewModelScope.launch {
            val list = diaryDao.getRecent(limit)
            val text = buildString {
                append("墨笺日记导出（最近${list.size}条）\n")
                append("导出时间：")
                append(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
                append("\n\n")
                list.forEachIndexed { idx, e ->
                    append("【${idx + 1}】\n")
                    append(formatDiary(e))
                    append("\n\n------------------------\n\n")
                }
            }
            onText(text)
        }
    }

    fun loadProfileStats() {
        viewModelScope.launch {
            val total = diaryDao.countAll()

            val (start, end) = monthRangeMillis(System.currentTimeMillis())
            val monthCount = diaryDao.countByRange(start, end)
            val avg = diaryDao.avgMoodByRange(start, end) ?: 0.0
            val monthDays = diaryDao.distinctDaysCountByRange(start, end)

            val streak = calcStreakDays()

            _profileStats.value = ProfileStats(
                totalCount = total,
                monthCount = monthCount,
                monthAvgMood = String.format(Locale.getDefault(), "%.2f", avg),
                monthDays = monthDays,
                streakDays = streak
            )
        }
    }

    // -------------------- helpers --------------------

    private fun formatDiary(e: DiaryEntity): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return buildString {
            append("标题：").append(e.title.ifBlank { "(无标题)" }).append("\n")
            append("时间：").append(sdf.format(Date(e.createdAt))).append("\n")
            append("心情：").append(e.mood).append("/5").append("\n")
            if (e.tagsText.isNotBlank()) append("标签：").append(e.tagsText).append("\n")
            if (e.isPinned) append("置顶：是\n")
            append("\n")
            append(e.content)
        }
    }

    private fun monthRangeMillis(anyMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = anyMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    /** 连续打卡：按“有记录的日期”倒序，连续相邻日期算 streak */
    private suspend fun calcStreakDays(): Int {
        val days = diaryDao.getDistinctDaysDesc(limit = 60) // 足够算连续
        if (days.isEmpty()) return 0

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fun parseDay(s: String): Long = fmt.parse(s)?.time ?: 0L

        var streak = 1
        for (i in 0 until days.size - 1) {
            val d1 = parseDay(days[i])
            val d2 = parseDay(days[i + 1])
            val diff = d1 - d2
            if (diff == 24L * 60 * 60 * 1000) streak++ else break
        }
        return streak
    }
}
