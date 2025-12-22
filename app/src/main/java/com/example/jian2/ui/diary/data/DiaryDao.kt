package com.example.jian2.ui.diary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryDao {
    // ✅ 统计：总日记数
    @Query("SELECT COUNT(*) FROM diary")
    suspend fun countAll(): Int

    // ✅ 统计：某段时间内日记数（用于“本月日记数”）
    @Query("SELECT COUNT(*) FROM diary WHERE createdAt >= :startMillis AND createdAt < :endMillis")
    suspend fun countByRange(startMillis: Long, endMillis: Long): Int

    // ✅ 统计：某段时间内平均心情（为空时返回 null）
    @Query("SELECT AVG(mood) FROM diary WHERE createdAt >= :startMillis AND createdAt < :endMillis")
    suspend fun avgMoodByRange(startMillis: Long, endMillis: Long): Double?

    // ✅ 导出：取最近 N 条
    @Query("SELECT * FROM diary ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DiaryEntity>


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

    @Query("SELECT * FROM diary WHERE title LIKE :kw OR content LIKE :kw ORDER BY isPinned DESC, createdAt DESC")
    suspend fun search(kw: String): List<DiaryEntity>

    // ✅ 新增：按时间范围取日记（用于“按天筛选”）
    @Query("""
        SELECT * FROM diary 
        WHERE createdAt >= :startMillis AND createdAt < :endMillis
        ORDER BY isPinned DESC, createdAt DESC
    """)
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<DiaryEntity>
}
