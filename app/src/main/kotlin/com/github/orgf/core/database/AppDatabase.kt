package com.github.orgf.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.orgf.core.database.dao.PromptTableDao
import com.github.orgf.core.database.models.PromptCategoryTable

@Database(
    entities = [
        PromptCategoryTable::class,
        PromptCategoryTable::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun promptTableDao(): PromptTableDao

}