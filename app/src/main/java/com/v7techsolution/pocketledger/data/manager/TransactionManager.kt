package com.v7techsolution.pocketledger.data.manager

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.entity.Transaction
import com.v7techsolution.pocketledger.data.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionManager @Inject constructor(
    private val dbHelper: SQLiteDatabaseHelper
) {

    fun getAllTransactions(): Flow<List<Transaction>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions",
            null,
            null,
            null,
            null,
            null,
            "date DESC"
        )
        
        val transactions = mutableListOf<Transaction>()
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(cursorToTransaction(it))
            }
        }
        emit(transactions)
    }.flowOn(Dispatchers.IO)

    fun getTransactionsFromDate(startDate: LocalDateTime): Flow<List<Transaction>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions",
            null,
            "date >= ?",
            arrayOf(dbHelper.localDateTimeToString(startDate)),
            null,
            null,
            "date DESC"
        )
        
        val transactions = mutableListOf<Transaction>()
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(cursorToTransaction(it))
            }
        }
        emit(transactions)
    }.flowOn(Dispatchers.IO)

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions",
            null,
            "category = ?",
            arrayOf(category),
            null,
            null,
            "date DESC"
        )
        
        val transactions = mutableListOf<Transaction>()
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(cursorToTransaction(it))
            }
        }
        emit(transactions)
    }.flowOn(Dispatchers.IO)

    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions",
            null,
            "account_id = ?",
            arrayOf(accountId.toString()),
            null,
            null,
            "date DESC"
        )
        
        val transactions = mutableListOf<Transaction>()
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(cursorToTransaction(it))
            }
        }
        emit(transactions)
    }.flowOn(Dispatchers.IO)

    suspend fun getTransactionById(id: Long): Transaction? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToTransaction(it)
            } else null
        }
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        val db = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                put("amount", dbHelper.bigDecimalToString(transaction.amount))
                put("type", transaction.type.name)
                put("category", transaction.category)
                put("description", transaction.description)
                put("account_id", transaction.accountId)
                put("date", dbHelper.localDateTimeToString(transaction.date))
                put("receipt_photo_path", transaction.receiptPhotoPath)
                put("is_recurring", if (transaction.isRecurring) 1 else 0)
                put("recurring_pattern", transaction.recurringPattern)
                put("created_at", dbHelper.localDateTimeToString(transaction.createdAt))
                put("updated_at", dbHelper.localDateTimeToString(transaction.updatedAt))
            }
            
            val result = db.insert("transactions", null, values)
            if (result == -1L) {
                android.util.Log.e("TransactionManager", "Failed to insert transaction: ${transaction}")
                throw Exception("Failed to insert transaction into database")
            }
            android.util.Log.d("TransactionManager", "Transaction inserted successfully with ID: $result")
            return result
        } catch (e: Exception) {
            android.util.Log.e("TransactionManager", "Error inserting transaction", e)
            throw e
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("amount", dbHelper.bigDecimalToString(transaction.amount))
            put("type", transaction.type.name)
            put("category", transaction.category)
            put("description", transaction.description)
            put("account_id", transaction.accountId)
            put("date", dbHelper.localDateTimeToString(transaction.date))
            put("receipt_photo_path", transaction.receiptPhotoPath)
            put("is_recurring", if (transaction.isRecurring) 1 else 0)
            put("recurring_pattern", transaction.recurringPattern)
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("transactions", values, "id = ?", arrayOf(transaction.id.toString()))
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        db.delete("transactions", "id = ?", arrayOf(transaction.id.toString()))
    }

    suspend fun getTotalExpensesFromDate(startDate: LocalDateTime): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(CAST(amount AS REAL)) FROM transactions WHERE type = 'EXPENSE' AND date >= ?",
            arrayOf(dbHelper.localDateTimeToString(startDate))
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                it.getDouble(0)
            } else 0.0
        }
    }

    suspend fun getTotalIncomeFromDate(startDate: LocalDateTime): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(CAST(amount AS REAL)) FROM transactions WHERE type = 'INCOME' AND date >= ?",
            arrayOf(dbHelper.localDateTimeToString(startDate))
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                it.getDouble(0)
            } else 0.0
        }
    }

    private fun cursorToTransaction(cursor: Cursor): Transaction {
        return Transaction(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            amount = dbHelper.stringToBigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("amount"))),
            type = TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))),
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
            accountId = cursor.getLong(cursor.getColumnIndexOrThrow("account_id")),
            date = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("date"))),
            receiptPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow("receipt_photo_path")),
            isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow("is_recurring")) == 1,
            recurringPattern = cursor.getString(cursor.getColumnIndexOrThrow("recurring_pattern")),
            createdAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("created_at"))),
            updatedAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")))
        )
    }

    suspend fun getRecentTransactions(limit: Int): List<Transaction> {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "transactions",
                null,
                null,
                null,
                null,
                null,
                "date DESC",
                limit.toString()
            )
            
            val transactions = mutableListOf<Transaction>()
            cursor.use {
                while (it.moveToNext()) {
                    transactions.add(cursorToTransaction(it))
                }
            }
            transactions
        } catch (e: Exception) {
            android.util.Log.e("TransactionManager", "Error getting recent transactions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTotalTransactionCount(): Int {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT COUNT(*) FROM transactions", null)
            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TransactionManager", "Error getting transaction count: ${e.message}")
            0
        }
    }
}
