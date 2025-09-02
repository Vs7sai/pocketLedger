package com.v7techsolution.pocketledger.data.manager

import android.content.ContentValues
import android.database.Cursor
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.entity.Account
import com.v7techsolution.pocketledger.data.entity.AccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManager @Inject constructor(
    private val dbHelper: SQLiteDatabaseHelper
) {

    fun getAllActiveAccounts(): Flow<List<Account>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "accounts",
            null,
            "is_active = 1",
            null,
            null,
            null,
            "name ASC"
        )
        
        val accounts = mutableListOf<Account>()
        cursor.use {
            while (it.moveToNext()) {
                accounts.add(cursorToAccount(it))
            }
        }
        emit(accounts)
    }.flowOn(Dispatchers.IO)

    suspend fun getAccountById(id: Long): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "accounts",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToAccount(it)
            } else null
        }
    }

    suspend fun insertAccount(account: Account): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", account.name)
            put("type", account.type.name)
            put("balance", dbHelper.bigDecimalToString(account.balance))
            put("currency", account.currency)
            put("color", account.color)
            put("icon", account.icon)
            put("is_active", if (account.isActive) 1 else 0)
            put("created_at", dbHelper.localDateTimeToString(account.createdAt))
            put("updated_at", dbHelper.localDateTimeToString(account.updatedAt))
        }
        
        return db.insert("accounts", null, values)
    }

    suspend fun updateAccount(account: Account) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", account.name)
            put("type", account.type.name)
            put("balance", dbHelper.bigDecimalToString(account.balance))
            put("currency", account.currency)
            put("color", account.color)
            put("icon", account.icon)
            put("is_active", if (account.isActive) 1 else 0)
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("accounts", values, "id = ?", arrayOf(account.id.toString()))
    }

    suspend fun deleteAccount(account: Account) {
        val db = dbHelper.writableDatabase
        db.delete("accounts", "id = ?", arrayOf(account.id.toString()))
    }

    suspend fun updateAccountBalance(accountId: Long, newBalance: BigDecimal) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("balance", dbHelper.bigDecimalToString(newBalance))
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("accounts", values, "id = ?", arrayOf(accountId.toString()))
    }

    suspend fun getTotalNetWorth(): BigDecimal {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(CAST(balance AS REAL)) FROM accounts WHERE is_active = 1",
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                val sum = it.getDouble(0)
                if (sum > 0) BigDecimal(sum.toString()) else BigDecimal.ZERO
            } else BigDecimal.ZERO
        }
    }

    private fun cursorToAccount(cursor: Cursor): Account {
        return Account(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            type = AccountType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))),
            balance = dbHelper.stringToBigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("balance"))),
            currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
            color = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
            icon = cursor.getString(cursor.getColumnIndexOrThrow("icon")),
            isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1,
            createdAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("created_at"))),
            updatedAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")))
        )
    }
}
