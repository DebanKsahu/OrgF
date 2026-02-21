package com.github.orgf.core.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.orgf.utils.enums.PromptCategory

@Entity
data class PromptCategoryTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(index = true)
    val categoryName: PromptCategory
)
