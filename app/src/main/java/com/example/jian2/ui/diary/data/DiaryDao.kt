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
