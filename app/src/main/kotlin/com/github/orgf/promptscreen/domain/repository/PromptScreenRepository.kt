package com.github.orgf.promptscreen.domain.repository

import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.utils.enums.PromptCategory

interface PromptScreenRepository {
    suspend fun getAllPrompts(): List<PromptCard>

    suspend fun getPromptCategoryById(categoryId: Long): PromptCategory

}