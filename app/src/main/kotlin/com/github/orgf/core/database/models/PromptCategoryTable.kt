package com.github.orgf.core.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.orgf.utils.enums.PromptCategory

@Entity(
    indices = [
        Index(value = ["categoryName"], unique = true)
    ]
)
data class PromptCategoryTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    @ColumnInfo
    val categoryName: PromptCategory
)
