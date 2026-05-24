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

    /*
    SQL operations for PromptCategoryTable.
    This sections divided into 4 parts:
    1. Insert Section (This contains all create operations)
    2. Read Section (This contains all read operations)
    3. Update Section (This contains all update operations)
    4. Delete Section (This contains all delete operations)
     */


    // ------------------- Insert Section -------------------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPromptCategoryDetail(promptCategory: PromptCategoryTable): Long

    @Transaction
    suspend fun getOrInsertPromptCategoryIdByName(categoryName: PromptCategory): Long {
        val insertedCategoryId = insertPromptCategoryDetail(
            promptCategory = PromptCategoryTable(categoryName = categoryName)
        )
        if (insertedCategoryId != -1L) {
            return insertedCategoryId
        }

        return getPromptCategoryIdByName(categoryName = categoryName)
            ?: error("Failed to resolve PromptCategory id for: $categoryName")
    }


    // ------------------- Read Section ---------------------

    @Query(
        """
            SELECT * FROM PromptCategoryTable 
            WHERE id = :categoryId
            LIMIT 1
        """
    )
    suspend fun getPromptCategoryDetailById(categoryId: Long): PromptCategoryTable?

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
            SELECT categoryName FROM PromptCategoryTable 
            WHERE id = :categoryId
        """
    )
    suspend fun getPromptCategoryNameById(categoryId: Long): PromptCategory?

    // ------------------- Update Section -------------------


    // ------------------- Delete Section -------------------

    @Query(
        """
            DELETE FROM PromptCategoryTable 
            WHERE id = :categoryId
        """
    )
    suspend fun deletePromptCategoryDetailById(categoryId: Long): Int

    suspend fun deletePromptCategoryDetailByName(categoryName: PromptCategory): Int {
        val categoryId = getPromptCategoryIdByName(categoryName = categoryName)
        return if (categoryId == null) 0 else deletePromptCategoryDetailById(categoryId = categoryId)
    }

    /*
    SQL operations for PromptCusterTable.
    This sections divided into 4 parts:
    1. Insert Section (This contains all create operations)
    2. Read Section (This contains all read operations)
    3. Update Section (This contains all update operations)
    4. Delete Section (This contains all delete operations)
     */

    // ------------------- Insert Section ---------------------

    @Insert
    suspend fun insertPromptClusterDetail(clusterData: PromptClusterTable): Long

    // ------------------- Read Section -----------------------

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE id = :clusterId
    """
    )
    suspend fun getPromptClusterDetailById(clusterId: Long): PromptClusterTable?

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE categoryId = :categoryId
        """
    )
    suspend fun getPromptClusterDetailByCategoryId(categoryId: Long): List<PromptClusterTable>

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE parentClusterId = :parentClusterId
    """
    )
    suspend fun getPromptClusterDetailByParentClusterId(parentClusterId: Long?): List<PromptClusterTable>

    @Query(
        """
        SELECT * FROM PromptClusterTable 
        WHERE categoryId = :categoryId 
        AND parentClusterId IS NULL
    """
    )
    suspend fun getLayer1PromptClusterDetailByCategoryId(categoryId: Long): List<PromptClusterTable>

    @Query(
        """
        SELECT * FROM PromptClusterTable
        WHERE text IS NOT NULL
    """
    )
    suspend fun getAllPromptDetail(): List<PromptClusterTable>

    @Query(
        """
            SELECT * FROM PromptClusterTable 
            WHERE categoryId = :categoryId
            AND text IS NOT NULL
        """
    )
    suspend fun getPromptDetailByCategoryId(categoryId: Long): List<PromptClusterTable>

    // ------------------- Update Section ---------------------

    @Update
    suspend fun updatePromptClusterDetail(newClusterData: PromptClusterTable)

    @Query(
        """
        UPDATE PromptClusterTable 
        SET isEnabled = :isActive 
        WHERE id = :promptId
    """
    )
    suspend fun updatePromptActiveStatus(promptId: Long, isActive: Boolean)

    // ------------------- Delete Section ---------------------

}