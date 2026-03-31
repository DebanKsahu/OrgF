package com.github.orgf.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.core.database.models.PromptClusterTable
import com.github.orgf.utils.enums.PromptCategory

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
    suspend fun getPromptCategory(categoryName: PromptCategory): PromptCategoryTable?

    @Query(
        """
            SELECT * FROM PromptCategoryTable 
            WHERE id = :categoryId
            LIMIT 1
        """
    )
    suspend fun getPromptCategoryById(categoryId: Long): PromptCategoryTable?

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

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE id = :clusterId
    """
    )
    suspend fun getPromptClusterById(clusterId: Long): PromptClusterTable?

    @Query(
        """
        SELECT * FROM PromptClusterTable
        WHERE text IS NOT NULL
    """
    )
    suspend fun getAllPrompts(): List<PromptClusterTable>


    @Insert
    suspend fun insertPrompt(clusterData: PromptClusterTable): Long

    @Update
    suspend fun updateCluster(newClusterData: PromptClusterTable)
}