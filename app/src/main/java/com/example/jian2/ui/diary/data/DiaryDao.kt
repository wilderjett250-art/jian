package com.example.jian2.ui.diary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    // ✅ 列表监听（你要的“列表页监听数据库”）
    @Query("SELECT * FROM diary ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<DiaryEntity>>




    @Query("SELECT * FROM diary WHERE createdAt >= :start AND createdAt < :end ORDER BY createdAt DESC")
    suspend fun getByRange(start: Long, end: Long): List<DiaryEntity>

    // ✅ 兼容你原本的 getAll（导出/一次性加载也能用）
    @Query("SELECT * FROM diary ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAll(): List<DiaryEntity>

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntity?

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<DiaryEntity?>

    @Insert
    suspend fun insert(entity: DiaryEntity): Long

    @Update
    suspend fun update(entity: DiaryEntity)

    @Query("DELETE FROM diary WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE diary SET isPinned = :pinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ✅ 高级搜索
    @Query("""
        SELECT * FROM diary
        WHERE (title LIKE :kw OR content LIKE :kw OR tagsText LIKE :kw)
          AND (:tag = '' OR tagsText LIKE '%' || :tag || '%')
          AND mood BETWEEN :moodMin AND :moodMax
        ORDER BY isPinned DESC, createdAt DESC
    """)
    suspend fun searchAdvanced(
        kw: String,
        tag: String,
        moodMin: Int,
        moodMax: Int
    ): List<DiaryEntity>

    // ✅ 日历范围
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

    @Query("""
        SELECT COUNT(DISTINCT date(createdAt/1000,'unixepoch','localtime'))
        FROM diary
        WHERE createdAt >= :startMillis AND createdAt < :endMillis
    """)
    suspend fun distinctDaysCountByRange(startMillis: Long, endMillis: Long): Int

    @Query("""
        SELECT DISTINCT date(createdAt/1000,'unixepoch','localtime') AS d
        FROM diary
        ORDER BY d DESC
        LIMIT :limit
    """)
    suspend fun getDistinctDaysDesc(limit: Int): List<String>

    @Query("SELECT * FROM diary ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DiaryEntity>
}
