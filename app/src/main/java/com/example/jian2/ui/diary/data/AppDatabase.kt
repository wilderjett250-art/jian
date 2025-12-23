package com.example.jian2.ui.diary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DiaryEntity::class],
    version = 2, // ✅ 升级：因为 DiaryEntity 加了 tagsText / coverUri
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jian_diary.db"
                )
                    .fallbackToDestructiveMigration() // ✅ 平替：省去写 migration，稳定不闪退（会清旧数据）
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
