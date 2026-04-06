package com.github.orgf.promptscreen.domain.repository


import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.domain.model.PromptDetailDomain
import com.github.orgf.utils.enums.PromptCategory

interface PromptScreenRepository {
    suspend fun getAllPrompts(): List<PromptCard>

    suspend fun getPromptCategoryById(categoryId: Long): PromptCategory

    suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean)

    suspend fun addPrompt(promptDetail: PromptDetailDomain): Long

}