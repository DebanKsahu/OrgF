package com.github.orgf.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.core.database.models.PromptClusterTable

@Dao
interface PromptTableDao {

    // PromptCategoryTable

    @Query(
        """
            SELECT * FROM PromptCategoryTable 
            WHERE categoryName = :categoryName
            LIMIT 1
        """
    )
    suspend fun getPromptCategory(categoryName: String): PromptCategoryTable?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPromptCategory(promptCategory: PromptCategoryTable): Long

    // PromptClusterTable

    @Query("""
        SELECT * FROM PromptClusterTable 
        WHERE parentClusterId = :parentClusterId
    """)
    suspend fun getPromptClustersByParentClusterId(parentClusterId: Long?): List<PromptClusterTable>

    @Query("""
        SELECT * FROM PromptClusterTable 
        WHERE categoryId = :categoryId 
        AND parentClusterId IS NULL
    """)
    suspend fun getTopLevelPromptClustersByCategoryId(categoryId: Long): List<PromptClusterTable>

    @Insert
    suspend fun insertPrompt(node: PromptClusterTable): Long

    @Update
    suspend fun updatePrompt(node: PromptClusterTable)
}