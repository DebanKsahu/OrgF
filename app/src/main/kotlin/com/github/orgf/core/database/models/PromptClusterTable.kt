package com.github.orgf.core.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PromptClusterTable::class,
            parentColumns = ["id"],
            childColumns = ["parentClusterId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = PromptCategoryTable::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PromptClusterTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(index = true)
    val categoryId: Long,

    val parentClusterId: Long?,

    val text: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val vectorEmbedding: FloatArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PromptClusterTable

        if (id != other.id) return false
        if (categoryId != other.categoryId) return false
        if (parentClusterId != other.parentClusterId) return false
        if (text != other.text) return false
        if (!vectorEmbedding.contentEquals(other.vectorEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + (parentClusterId?.hashCode() ?: 0)
        result = 31 * result + text.hashCode()
        result = 31 * result + (vectorEmbedding?.contentHashCode() ?: 0)
        return result
    }
}
