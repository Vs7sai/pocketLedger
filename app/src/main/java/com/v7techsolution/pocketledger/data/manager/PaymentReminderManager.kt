package com.v7techsolution.pocketledger.data.manager

import android.content.ContentValues
import android.database.Cursor
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.entity.PaymentReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentReminderManager @Inject constructor(
    private val dbHelper: SQLiteDatabaseHelper
) {

    fun getAllActiveReminders(): Flow<List<PaymentReminder>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "payment_reminders",
            null,
            "is_active = 1",
            null,
            null,
            null,
            "due_date ASC"
        )
        
        val reminders = mutableListOf<PaymentReminder>()
        cursor.use {
            while (it.moveToNext()) {
                reminders.add(cursorToPaymentReminder(it))
            }
        }
        emit(reminders)
    }.flowOn(Dispatchers.IO)

    fun getRemindersInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PaymentReminder>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "payment_reminders",
            null,
            "due_date BETWEEN ? AND ? AND is_active = 1",
            arrayOf(
                dbHelper.localDateTimeToString(startDate),
                dbHelper.localDateTimeToString(endDate)
            ),
            null,
            null,
            "due_date ASC"
        )
        
        val reminders = mutableListOf<PaymentReminder>()
        cursor.use {
            while (it.moveToNext()) {
                reminders.add(cursorToPaymentReminder(it))
            }
        }
        emit(reminders)
    }.flowOn(Dispatchers.IO)

    fun getUpcomingReminders(days: Int = 7): Flow<List<PaymentReminder>> = flow {
        val db = dbHelper.readableDatabase
        val futureDate = LocalDateTime.now().plusDays(days.toLong())
        val cursor = db.query(
            "payment_reminders",
            null,
            "due_date <= ? AND is_active = 1 AND is_paid = 0",
            arrayOf(dbHelper.localDateTimeToString(futureDate)),
            null,
            null,
            "due_date ASC"
        )
        
        val reminders = mutableListOf<PaymentReminder>()
        cursor.use {
            while (it.moveToNext()) {
                reminders.add(cursorToPaymentReminder(it))
            }
        }
        emit(reminders)
    }.flowOn(Dispatchers.IO)

    suspend fun getReminderById(id: Long): PaymentReminder? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "payment_reminders",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToPaymentReminder(it)
            } else null
        }
    }

    suspend fun insertReminder(reminder: PaymentReminder): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", reminder.title)
            put("amount", dbHelper.bigDecimalToString(reminder.amount))
            put("due_date", dbHelper.localDateTimeToString(reminder.dueDate))
            put("category_id", reminder.categoryId)
            put("account_id", reminder.accountId)
            put("description", reminder.description)
            put("is_recurring", if (reminder.isRecurring) 1 else 0)
            put("recurring_pattern", reminder.recurringPattern)
            put("reminder_days", reminder.reminderDays)
            put("is_active", if (reminder.isActive) 1 else 0)
            put("is_paid", if (reminder.isPaid) 1 else 0)
            put("created_at", dbHelper.localDateTimeToString(reminder.createdAt))
            put("updated_at", dbHelper.localDateTimeToString(reminder.updatedAt))
        }
        
        return db.insert("payment_reminders", null, values)
    }

    suspend fun updateReminder(reminder: PaymentReminder) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", reminder.title)
            put("amount", dbHelper.bigDecimalToString(reminder.amount))
            put("due_date", dbHelper.localDateTimeToString(reminder.dueDate))
            put("category_id", reminder.categoryId)
            put("account_id", reminder.accountId)
            put("description", reminder.description)
            put("is_recurring", if (reminder.isRecurring) 1 else 0)
            put("recurring_pattern", reminder.recurringPattern)
            put("reminder_days", reminder.reminderDays)
            put("is_active", if (reminder.isActive) 1 else 0)
            put("is_paid", if (reminder.isPaid) 1 else 0)
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("payment_reminders", values, "id = ?", arrayOf(reminder.id.toString()))
    }

    suspend fun deleteReminder(reminder: PaymentReminder) {
        val db = dbHelper.writableDatabase
        db.delete("payment_reminders", "id = ?", arrayOf(reminder.id.toString()))
    }

    suspend fun markAsPaid(reminderId: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_paid", 1)
            put("updated_at", dbHelper.localDateTimeToString(LocalDateTime.now()))
        }
        
        db.update("payment_reminders", values, "id = ?", arrayOf(reminderId.toString()))
    }

    suspend fun getRemindersForNotification(): List<PaymentReminder> {
        val db = dbHelper.readableDatabase
        val threeDaysFromNow = LocalDateTime.now().plusDays(3)
        val cursor = db.query(
            "payment_reminders",
            null,
            "due_date <= ? AND is_active = 1 AND is_paid = 0",
            arrayOf(dbHelper.localDateTimeToString(threeDaysFromNow)),
            null,
            null,
            "due_date ASC"
        )
        
        val reminders = mutableListOf<PaymentReminder>()
        cursor.use {
            while (it.moveToNext()) {
                reminders.add(cursorToPaymentReminder(it))
            }
        }
        return reminders
    }

    private fun cursorToPaymentReminder(cursor: Cursor): PaymentReminder {
        return PaymentReminder(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            amount = dbHelper.stringToBigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("amount"))),
            dueDate = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("due_date"))),
            categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
            accountId = cursor.getLong(cursor.getColumnIndexOrThrow("account_id")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
            isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow("is_recurring")) == 1,
            recurringPattern = cursor.getString(cursor.getColumnIndexOrThrow("recurring_pattern")),
            reminderDays = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_days")),
            isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1,
            isPaid = cursor.getInt(cursor.getColumnIndexOrThrow("is_paid")) == 1,
            createdAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("created_at"))),
            updatedAt = dbHelper.stringToLocalDateTime(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")))
        )
    }
}
