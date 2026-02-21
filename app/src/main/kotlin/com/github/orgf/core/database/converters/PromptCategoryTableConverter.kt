package com.github.orgf.core.database.converters

import androidx.room.TypeConverter
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.utils.enums.PromptCategory

class PromptCategoryTableConverter {

    @TypeConverter
    fun fromPromptCategory(promptCategory: PromptCategory): String {
        return promptCategory.name
    }

    @TypeConverter
    fun toPromptCategory(promptCategory: String): PromptCategory {
        return PromptCategory.valueOf(promptCategory)
    }
}