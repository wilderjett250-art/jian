package com.example.jian2.ui.diary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DiaryEntity>>

    @Insert
    suspend fun insert(entity: DiaryEntity)

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntity?
}
