package com.github.orgf.promptscreen.data.mapper

import androidx.compose.ui.text.AnnotatedString
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.models.PromptClusterTable
import com.github.orgf.promptscreen.domain.model.PromptCard

suspend fun PromptClusterTable.toPromptCard(appDatabase: AppDatabase): PromptCard {
    return if (this.id != null && this.text != null) {
        val promptCategory = try {
            val promptCategoryDetail =
                appDatabase.promptTableDao().getPromptCategoryById(categoryId)
            promptCategoryDetail?.categoryName
                ?: error("There is no prompt category with id: $categoryId")
        } catch (e: Exception) {
            error("Failed to get prompt category with id: $categoryId. Error: ${e.message}")
        }
        PromptCard(
            promptId = this.id,
            promptText = AnnotatedString(this.text),
            promptCategory = promptCategory.toString(),
            isEnabled = true
        )
    } else {
        throw IllegalArgumentException("A valid prompt should have a not null id and text")
    }
}

suspend fun List<PromptClusterTable>.toPromptCardList(appDatabase: AppDatabase): List<PromptCard> {
    val resultList = mutableListOf<PromptCard>()
    for (promptRow in this) {
        resultList.add(promptRow.toPromptCard(appDatabase = appDatabase))
    }
    return resultList.toList()
}