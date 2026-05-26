package com.github.orgf.promptscreen.domain.repository


import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.domain.model.PromptDetail
import com.github.orgf.utils.enums.PromptCategory

interface PromptScreenRepository {
    suspend fun getAllPromptDetail(): List<PromptCard>

    suspend fun getPromptCategoryDetailById(categoryId: Long): PromptCategory

    suspend fun getPromptDetailByCategoryName(category: PromptCategory): List<PromptCard>

    suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean)

    suspend fun addPromptDetail(promptDetail: PromptDetail): Long

}