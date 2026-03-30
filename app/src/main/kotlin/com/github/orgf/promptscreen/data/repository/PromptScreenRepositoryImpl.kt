package com.github.orgf.promptscreen.data.repository

import com.github.orgf.core.database.AppDatabase
import com.github.orgf.promptscreen.data.mapper.toPromptCardList
import com.github.orgf.promptscreen.domain.model.PromptCard
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.utils.enums.PromptCategory

class PromptScreenRepositoryImpl(
    private val appDatabase: AppDatabase
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
                ?: throw error("There is no prompt category with id: $categoryId")
        } catch (e: Exception) {
            throw error("Failed to get prompt category with id: $categoryId. Error: ${e.message}")
        }
    }
}