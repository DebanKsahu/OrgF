package com.github.orgf.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.orgf.core.database.converters.PromptCategoryTableConverter
import com.github.orgf.core.database.converters.PromptClusterTableConverter
import com.github.orgf.core.database.dao.PromptTableDao
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.core.database.models.PromptClusterTable

@Database(
    entities = [
        PromptCategoryTable::class,
        PromptClusterTable::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    PromptCategoryTableConverter::class,
    PromptClusterTableConverter::class
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun promptTableDao(): PromptTableDao

}