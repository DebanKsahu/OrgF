package com.github.orgf.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.github.orgf.core.database.models.PromptCategoryTable
import com.github.orgf.core.database.models.PromptClusterTable
import com.github.orgf.utils.enums.PromptCategory

@Dao
interface PromptTableDao {

    // PromptCategoryTable

    @Query(
        """
            SELECT id FROM PromptCategoryTable 
            WHERE categoryName = :categoryName
            LIMIT 1
        """
    )
    suspend fun getPromptCategoryIdByName(categoryName: PromptCategory): Long?

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

    @Transaction
    suspend fun getOrCreatePromptCategoryId(categoryName: PromptCategory): Long {
        val insertedCategoryId = insertPromptCategory(
            promptCategory = PromptCategoryTable(categoryName = categoryName)
        )
        if (insertedCategoryId != -1L) {
            return insertedCategoryId
        }

        return getPromptCategoryIdByName(categoryName = categoryName)
            ?: error("Failed to resolve PromptCategory id for: $categoryName")
    }

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
        WHERE text IS NOT NULL
    """
    )
    suspend fun getAllPrompts(): List<PromptClusterTable>

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE id = :clusterId
    """
    )
    suspend fun getPromptById(clusterId: Long): PromptClusterTable?

    @Query(
        """
            SELECT * FROM PromptClusterTable 
            WHERE categoryId = :categoryId
            AND text IS NOT NULL
        """
    )
    suspend fun getPromptByCategory(categoryId: Long): List<PromptClusterTable>


    @Insert
    suspend fun insertPromptCluster(clusterData: PromptClusterTable): Long

    @Update
    suspend fun updateCluster(newClusterData: PromptClusterTable)

    @Query(
        """
        UPDATE PromptClusterTable 
        SET isEnabled = :isActive 
        WHERE id = :promptId
    """
    )
    suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean)

}