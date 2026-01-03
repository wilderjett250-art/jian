package com.example.jian2.ui.diary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryMediaDao {

    @Query("SELECT * FROM diary_media WHERE diaryId = :diaryId ORDER BY id DESC")
    fun observeByDiaryId(diaryId: Long): Flow<List<DiaryMediaEntity>>

    @Query("SELECT * FROM diary_media WHERE diaryId = :diaryId ORDER BY id DESC")
    suspend fun getByDiaryId(diaryId: Long): List<DiaryMediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DiaryMediaEntity>)

    @Query("DELETE FROM diary_media WHERE diaryId = :diaryId")
    suspend fun deleteByDiaryId(diaryId: Long)
}
