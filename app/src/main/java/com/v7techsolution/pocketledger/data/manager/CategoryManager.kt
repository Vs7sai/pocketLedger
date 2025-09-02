package com.v7techsolution.pocketledger.data.manager

import android.content.ContentValues
import android.database.Cursor
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.entity.Category
import com.v7techsolution.pocketledger.data.entity.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryManager @Inject constructor(
    private val dbHelper: SQLiteDatabaseHelper
) {

    fun getAllCategories(): Flow<List<Category>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "categories",
            null,
            null,
            null,
            null,
            null,
            "usage_count DESC, name ASC"
        )
        
        val categories = mutableListOf<Category>()
        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        emit(categories)
    }.flowOn(Dispatchers.IO)

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "categories",
            null,
            "type = ?",
            arrayOf(type.name),
            null,
            null,
            "usage_count DESC, name ASC"
        )
        
        val categories = mutableListOf<Category>()
        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        emit(categories)
    }.flowOn(Dispatchers.IO)

    suspend fun getCategoryById(id: Long): Category? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "categories",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToCategory(it)
            } else null
        }
    }

    suspend fun insertCategory(category: Category): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", category.name)
            put("color", category.color)
            put("icon", category.icon)
            put("type", category.type.name)
            put("is_default", if (category.isDefault) 1 else 0)
            put("usage_count", category.usageCount)
            put("last_used", category.lastUsed?.let { dbHelper.localDateTimeToString(it) })
            put("created_at", dbHelper.localDateTimeToString(category.createdAt))
            put("updated_at", dbHelper.localDateTimeToString(category.updatedAt))
        }
        
        return db.insert("categories", null, values)
    }

    suspend fun updateCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", category.name)
            put("color", category.color)
            put("icon", category.icon)
            put("type", category.type.name)
            put("is_default", if (category.isDefault) 1 else 0)
            put("usage_count", category.usageCount)
            put("last_used", category.lastUsed?.let { dbHelper.localDateTimeToString(it) })
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("categories", values, "id = ?", arrayOf(category.id.toString()))
    }

    suspend fun deleteCategory(category: Category) {
        val db = dbHelper.writableDatabase
        db.delete("categories", "id = ?", arrayOf(category.id.toString()))
    }

    suspend fun incrementUsageCount(categoryId: Long, timestamp: LocalDateTime) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("usage_count", "usage_count + 1")
            put("last_used", dbHelper.localDateTimeToString(timestamp))
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("categories", values, "id = ?", arrayOf(categoryId.toString()))
    }

    suspend fun searchCategories(query: String): List<Category> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "categories",
            null,
            "name LIKE ?",
            arrayOf("%$query%"),
            null,
            null,
            "usage_count DESC"
        )
        
        val categories = mutableListOf<Category>()
        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        return categories
    }

    private fun cursorToCategory(cursor: Cursor): Category {
        return Category(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            color = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
            icon = cursor.getString(cursor.getColumnIndexOrThrow("icon")),
            type = CategoryType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))),
            isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1,
            usageCount = cursor.getInt(cursor.getColumnIndexOrThrow("usage_count")),
            lastUsed = cursor.getString(cursor.getColumnIndexOrThrow("last_used"))?.let { 
                dbHelper.stringToLocalDateTime(it) 
            },
            createdAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("created_at"))),
            updatedAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")))
        )
    }
}
