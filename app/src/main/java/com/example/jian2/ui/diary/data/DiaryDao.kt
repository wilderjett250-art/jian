package com.example.jian2.ui.diary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAll(): List<DiaryEntity>

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntity?

    @Insert
    suspend fun insert(entity: DiaryEntity): Long

    @Update
    suspend fun update(entity: DiaryEntity)

    @Query("DELETE FROM diary WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE diary SET isPinned = :pinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ✅ 高级搜索：关键词（标题/正文/标签） + 标签筛选 + 心情范围
    @Query("""
        SELECT * FROM diary
        WHERE (title LIKE :kw OR content LIKE :kw OR tagsText LIKE :kw)
          AND (:tag = '' OR tagsText LIKE '%' || :tag || '%')
          AND mood BETWEEN :moodMin AND :moodMax
        ORDER BY isPinned DESC, createdAt DESC
    """)
    suspend fun searchAdvanced(kw: String, tag: String, moodMin: Int, moodMax: Int): List<DiaryEntity>

    // ✅ 日历按天：时间范围取日记
    @Query("""
        SELECT * FROM diary
        WHERE createdAt >= :startMillis AND createdAt < :endMillis
        ORDER BY isPinned DESC, createdAt DESC
    """)
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<DiaryEntity>

    // ---- 统计 ----
    @Query("SELECT COUNT(*) FROM diary")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM diary WHERE createdAt >= :startMillis AND createdAt < :endMillis")
    suspend fun countByRange(startMillis: Long, endMillis: Long): Int

    @Query("SELECT AVG(mood) FROM diary WHERE createdAt >= :startMillis AND createdAt < :endMillis")
    suspend fun avgMoodByRange(startMillis: Long, endMillis: Long): Double?

    // ✅ 本月记录天数（distinct days）
    @Query("""
        SELECT COUNT(DISTINCT date(createdAt/1000,'unixepoch','localtime'))
        FROM diary
        WHERE createdAt >= :startMillis AND createdAt < :endMillis
    """)
    suspend fun distinctDaysCountByRange(startMillis: Long, endMillis: Long): Int

    // ✅ streak：取最近若干天（去重后的日期字符串）
    @Query("""
        SELECT DISTINCT date(createdAt/1000,'unixepoch','localtime') AS d
        FROM diary
        ORDER BY d DESC
        LIMIT :limit
    """)
    suspend fun getDistinctDaysDesc(limit: Int): List<String>

    // ✅ 导出：最近 N 条
    @Query("SELECT * FROM diary ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DiaryEntity>
}
