package com.example.jian2.ui.diary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DiaryDao {

    @Insert
    suspend fun insert(diary: DiaryEntity)

    @Query("SELECT * FROM diary ORDER BY createTime DESC")
    suspend fun getAll(): List<DiaryEntity>
}
