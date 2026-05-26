package com.github.orgf.promptscreen.domain.model

import androidx.compose.ui.text.AnnotatedString
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.models.PromptClusterTable

data class PromptCard(
    val promptId: Long,
    val promptText: AnnotatedString,
    val promptCategory: String,
    val isEnabled: Boolean,
)

suspend fun PromptClusterTable.toPromptCard(appDatabase: AppDatabase): PromptCard {
    return if (this.id != null && this.text != null) {
        val promptCategory = try {
            val promptCategoryDetail =
                appDatabase.promptTableDao().getPromptCategoryDetailById(categoryId)
            promptCategoryDetail?.categoryName
                ?: error("There is no prompt category with id: $categoryId")
        } catch (e: Exception) {
            error("Failed to get prompt category with id: $categoryId. Error: ${e.message}")
        }
        PromptCard(
            promptId = this.id,
            promptText = AnnotatedString(this.text),
            promptCategory = promptCategory.toString(),
            isEnabled = this.isEnabled
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