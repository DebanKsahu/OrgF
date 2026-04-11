package com.github.orgf.promptscreen.data.repository

import com.github.orgf.core.agent.models.PromptDetail
import com.github.orgf.core.agent.prompt.PromptManager
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.promptscreen.data.mapper.toPromptCardList
import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.domain.model.PromptDetailDomain
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.utils.enums.PromptCategory

class PromptScreenRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val promptManager: PromptManager
) : PromptScreenRepository {
    override suspend fun getAllPrompts(): List<PromptCard> {
        return appDatabase.promptTableDao().getAllPrompts()
            .toPromptCardList(appDatabase = appDatabase)
    }

    override suspend fun getPromptCategoryById(categoryId: Long): PromptCategory {
        return try {
            val promptCategoryDetail =
                appDatabase.promptTableDao().getPromptCategoryById(categoryId)
            promptCategoryDetail?.categoryName
                ?: error("There is no prompt category with id: $categoryId")
        } catch (e: Exception) {
            error("Failed to get prompt category with id: $categoryId. Error: ${e.message}")
        }
    }

    override suspend fun getPromptsByCategory(category: PromptCategory): List<PromptCard> {
        val categoryId =
            appDatabase.promptTableDao().getPromptCategoryIdByName(categoryName = category) ?: -1
        val result = appDatabase.promptTableDao()
            .getPromptByCategory(categoryId = categoryId)
            .toPromptCardList(appDatabase = appDatabase)
        return result
    }

    override suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean) {
        appDatabase.promptTableDao().updatePromptActiveStatus(promptId, isActive)
    }

    override suspend fun addPrompt(promptDetail: PromptDetailDomain): Long {
        val promptClusterId = promptManager.addPrompt(
            promptDetail = PromptDetail(
                category = promptDetail.category,
                prompt = promptDetail.prompt,
                destinationFolder = promptDetail.destinationFolder
            )
        )
        return promptClusterId
    }
}