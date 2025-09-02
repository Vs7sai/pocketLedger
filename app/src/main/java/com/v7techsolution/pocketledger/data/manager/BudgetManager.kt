package com.v7techsolution.pocketledger.data.manager

import android.content.ContentValues
import android.database.Cursor
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.entity.Budget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetManager @Inject constructor(
    private val dbHelper: SQLiteDatabaseHelper
) {

    fun getAllActiveBudgets(): Flow<List<Budget>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "budgets",
            null,
            "is_active = 1",
            null,
            null,
            null,
            "month DESC"
        )
        
        val budgets = mutableListOf<Budget>()
        cursor.use {
            while (it.moveToNext()) {
                budgets.add(cursorToBudget(it))
            }
        }
        emit(budgets)
    }.flowOn(Dispatchers.IO)

    fun getBudgetsByMonth(month: YearMonth): Flow<List<Budget>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "budgets",
            null,
            "month = ? AND is_active = 1",
            arrayOf(dbHelper.yearMonthToString(month)),
            null,
            null,
            null
        )
        
        val budgets = mutableListOf<Budget>()
        cursor.use {
            while (it.moveToNext()) {
                budgets.add(cursorToBudget(it))
            }
        }
        emit(budgets)
    }.flowOn(Dispatchers.IO)

    suspend fun getBudgetById(id: Long): Budget? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "budgets",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToBudget(it)
            } else null
        }
    }

    suspend fun insertBudget(budget: Budget): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category_id", budget.categoryId)
            put("amount", dbHelper.bigDecimalToString(budget.amount))
            put("month", dbHelper.yearMonthToString(budget.month))
            put("spent", dbHelper.bigDecimalToString(budget.spent))
            put("is_active", if (budget.isActive) 1 else 0)
            put("created_at", dbHelper.localDateTimeToString(budget.createdAt))
            put("updated_at", dbHelper.localDateTimeToString(budget.updatedAt))
        }
        
        return db.insert("budgets", null, values)
    }

    suspend fun updateBudget(budget: Budget) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category_id", budget.categoryId)
            put("amount", dbHelper.bigDecimalToString(budget.amount))
            put("month", dbHelper.yearMonthToString(budget.month))
            put("spent", dbHelper.bigDecimalToString(budget.spent))
            put("is_active", if (budget.isActive) 1 else 0)
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("budgets", values, "id = ?", arrayOf(budget.id.toString()))
    }

    suspend fun deleteBudget(budget: Budget) {
        val db = dbHelper.writableDatabase
        db.delete("budgets", "id = ?", arrayOf(budget.id.toString()))
    }

    suspend fun updateBudgetSpent(budgetId: Long, newSpent: BigDecimal) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("spent", dbHelper.bigDecimalToString(newSpent))
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("budgets", values, "id = ?", arrayOf(budgetId.toString()))
    }

    private fun cursorToBudget(cursor: Cursor): Budget {
        return Budget(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
            amount = dbHelper.stringToBigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("amount"))),
            month = dbHelper.stringToYearMonth(cursor.getString(cursor.getColumnIndexOrThrow("month"))),
            spent = dbHelper.stringToBigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("spent"))),
            isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1,
            createdAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("created_at"))),
            updatedAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")))
        )
    }
}
