package com.example.jian2.ui.diary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DiaryEntity::class],
    version = 2,
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
                    // ✅ schema 不一致直接删库重建（避免启动闪退）
                    .fallbackToDestructiveMigration()
                    // ✅ 先止血：允许主线程查询（等稳定后再改回 IO）
                    .allowMainThreadQueries()
                    .build()


                INSTANCE = db
                db
            }
        }
    }
}
