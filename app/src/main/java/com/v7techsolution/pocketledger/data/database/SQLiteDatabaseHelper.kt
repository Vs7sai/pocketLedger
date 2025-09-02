package com.v7techsolution.pocketledger.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.v7techsolution.pocketledger.data.entity.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SQLiteDatabaseHelper @Inject constructor(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pocketledger.db"
        private const val DATABASE_VERSION = 5
        private const val TAG = "SQLiteDatabaseHelper"
    }

    init {
        Log.d(TAG, "Initializing SQLiteDatabaseHelper")
        // Force database creation/upgrade
        try {
            writableDatabase
            Log.d(TAG, "Database initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database", e)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating database tables")
        try {
            // Create tables
            createTables(db)
            insertDefaultData(db)
            Log.d(TAG, "Database tables created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database tables", e)
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading database from version $oldVersion to $newVersion")
        try {
            // Drop and recreate tables for now
            db.execSQL("DROP TABLE IF EXISTS transactions")
            db.execSQL("DROP TABLE IF EXISTS accounts")
            db.execSQL("DROP TABLE IF EXISTS categories")
            db.execSQL("DROP TABLE IF EXISTS budgets")
            db.execSQL("DROP TABLE IF EXISTS payment_reminders")
            onCreate(db)
            Log.d(TAG, "Database upgrade completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error upgrading database", e)
            throw e
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        Log.d(TAG, "Creating database tables")
        
        // Transactions table
        db.execSQL("""
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount TEXT NOT NULL,
                type TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                account_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                receipt_photo_path TEXT,
                is_recurring INTEGER DEFAULT 0,
                recurring_pattern TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())
        Log.d(TAG, "Transactions table created")
        
        // Verify table creation
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='transactions'", null)
        val tableExists = cursor.use { it.count > 0 }
        Log.d(TAG, "Transactions table exists: $tableExists")
        if (!tableExists) {
            throw Exception("Failed to create transactions table")
        }

        // Accounts table
        db.execSQL("""
            CREATE TABLE accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balance TEXT NOT NULL,
                currency TEXT DEFAULT 'USD',
                color INTEGER NOT NULL,
                icon TEXT NOT NULL,
                is_active INTEGER DEFAULT 1,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())
        Log.d(TAG, "Accounts table created")

        // Categories table
        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                color INTEGER NOT NULL,
                icon TEXT NOT NULL,
                type TEXT NOT NULL,
                is_default INTEGER DEFAULT 0,
                usage_count INTEGER DEFAULT 0,
                last_used TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())
        Log.d(TAG, "Categories table created")

        // Budgets table
        db.execSQL("""
            CREATE TABLE budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                amount TEXT NOT NULL,
                month TEXT NOT NULL,
                spent TEXT DEFAULT '0',
                is_active INTEGER DEFAULT 1,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())
        Log.d(TAG, "Budgets table created")

        // Payment Reminders table
        db.execSQL("""
            CREATE TABLE payment_reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount TEXT NOT NULL,
                due_date TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                description TEXT,
                is_recurring INTEGER DEFAULT 0,
                recurring_pattern TEXT,
                reminder_days INTEGER DEFAULT 3,
                is_active INTEGER DEFAULT 1,
                is_paid INTEGER DEFAULT 0,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
        """.trimIndent())
        Log.d(TAG, "Payment reminders table created")
    }

    private fun insertDefaultData(db: SQLiteDatabase) {
        Log.d(TAG, "Inserting default data")
        try {
            // Insert default categories - MOST IMPORTANT FIRST
            val defaultCategories = listOf(
                // HIGHEST PRIORITY - Essential Daily Expenses
                Category(name = "Food & Dining", color = 0xFFE57373.toInt(), icon = "🍽️", type = CategoryType.EXPENSE),
                Category(name = "Groceries", color = 0xFF81C784.toInt(), icon = "🛒", type = CategoryType.EXPENSE),
                Category(name = "Transportation", color = 0xFF81C784.toInt(), icon = "🚗", type = CategoryType.EXPENSE),
                Category(name = "Gas & Fuel", color = 0xFF795548.toInt(), icon = "⛽", type = CategoryType.EXPENSE),
                Category(name = "Housing", color = 0xFFFF8A65.toInt(), icon = "🏠", type = CategoryType.EXPENSE),
                Category(name = "Rent", color = 0xFFFF5722.toInt(), icon = "🏘️", type = CategoryType.EXPENSE),
                Category(name = "Utilities", color = 0xFFBA68C8.toInt(), icon = "⚡", type = CategoryType.EXPENSE),
                Category(name = "Electricity", color = 0xFFFFEB3B.toInt(), icon = "💡", type = CategoryType.EXPENSE),
                Category(name = "Internet", color = 0xFF3F51B5.toInt(), icon = "🌐", type = CategoryType.EXPENSE),
                Category(name = "Phone Bill", color = 0xFF9C27B0.toInt(), icon = "📞", type = CategoryType.EXPENSE),
                Category(name = "Healthcare", color = 0xFFF06292.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Shopping", color = 0xFF64B5F6.toInt(), icon = "🛍️", type = CategoryType.EXPENSE),
                Category(name = "Clothing & Shoes", color = 0xFFE1BEE7.toInt(), icon = "👕", type = CategoryType.EXPENSE),
                Category(name = "Entertainment", color = 0xFFFFB74D.toInt(), icon = "🎬", type = CategoryType.EXPENSE),
                Category(name = "Netflix", color = 0xFFE91E63.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Spotify", color = 0xFF4CAF50.toInt(), icon = "🎵", type = CategoryType.EXPENSE),
                Category(name = "Amazon Prime", color = 0xFFFF9800.toInt(), icon = "📦", type = CategoryType.EXPENSE),
                Category(name = "Education", color = 0xFF4FC3F7.toInt(), icon = "📚", type = CategoryType.EXPENSE),
                Category(name = "Taxes", color = 0xFF4DB6AC.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                Category(name = "Bank Fees", color = 0xFF795548.toInt(), icon = "🏦", type = CategoryType.EXPENSE),
                Category(name = "Credit Card Fees", color = 0xFFE91E63.toInt(), icon = "💳", type = CategoryType.EXPENSE),
                
                // SECOND PRIORITY - Common Daily Expenses
                Category(name = "Coffee & Snacks", color = 0xFF8D6E63.toInt(), icon = "☕", type = CategoryType.EXPENSE),
                Category(name = "Restaurants", color = 0xFFE57373.toInt(), icon = "🍕", type = CategoryType.EXPENSE),
                Category(name = "Fast Food", color = 0xFFFF7043.toInt(), icon = "🍔", type = CategoryType.EXPENSE),
                
                // Transportation
                Category(name = "Transportation", color = 0xFF81C784.toInt(), icon = "🚗", type = CategoryType.EXPENSE),
                Category(name = "Fuel/Gas", color = 0xFF795548.toInt(), icon = "⛽", type = CategoryType.EXPENSE),
                Category(name = "Public Transit", color = 0xFF607D8B.toInt(), icon = "🚌", type = CategoryType.EXPENSE),
                Category(name = "Ride Sharing", color = 0xFF9C27B0.toInt(), icon = "🚕", type = CategoryType.EXPENSE),
                Category(name = "Parking", color = 0xFF795548.toInt(), icon = "🅿️", type = CategoryType.EXPENSE),
                Category(name = "Car Maintenance", color = 0xFF424242.toInt(), icon = "🔧", type = CategoryType.EXPENSE),
                
                // Shopping & Lifestyle
                Category(name = "Shopping", color = 0xFF64B5F6.toInt(), icon = "🛍️", type = CategoryType.EXPENSE),
                Category(name = "Clothing", color = 0xFFE1BEE7.toInt(), icon = "👕", type = CategoryType.EXPENSE),
                Category(name = "Electronics", color = 0xFF90A4AE.toInt(), icon = "📱", type = CategoryType.EXPENSE),
                Category(name = "Home & Garden", color = 0xFF8BC34A.toInt(), icon = "🏡", type = CategoryType.EXPENSE),
                Category(name = "Books & Media", color = 0xFF795548.toInt(), icon = "📚", type = CategoryType.EXPENSE),
                Category(name = "Sports & Fitness", color = 0xFF4CAF50.toInt(), icon = "🏃", type = CategoryType.EXPENSE),
                
                // Entertainment & Leisure
                Category(name = "Entertainment", color = 0xFFFFB74D.toInt(), icon = "🎬", type = CategoryType.EXPENSE),
                Category(name = "Movies & Shows", color = 0xFFE91E63.toInt(), icon = "🎭", type = CategoryType.EXPENSE),
                Category(name = "Gaming", color = 0xFF9C27B0.toInt(), icon = "🎮", type = CategoryType.EXPENSE),
                Category(name = "Hobbies", color = 0xFFFF9800.toInt(), icon = "🎨", type = CategoryType.EXPENSE),
                Category(name = "Sports Events", color = 0xFF4CAF50.toInt(), icon = "⚽", type = CategoryType.EXPENSE),
                Category(name = "Concerts & Events", color = 0xFFE91E63.toInt(), icon = "🎵", type = CategoryType.EXPENSE),
                
                // Home & Utilities
                Category(name = "Utilities", color = 0xFFBA68C8.toInt(), icon = "⚡", type = CategoryType.EXPENSE),
                Category(name = "Electricity", color = 0xFFFFEB3B.toInt(), icon = "💡", type = CategoryType.EXPENSE),
                Category(name = "Water", color = 0xFF2196F3.toInt(), icon = "💧", type = CategoryType.EXPENSE),
                Category(name = "Internet", color = 0xFF3F51B5.toInt(), icon = "🌐", type = CategoryType.EXPENSE),
                Category(name = "Phone Bill", color = 0xFF9C27B0.toInt(), icon = "📞", type = CategoryType.EXPENSE),
                Category(name = "Cable TV", color = 0xFFE91E63.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Housing", color = 0xFFFF8A65.toInt(), icon = "🏠", type = CategoryType.EXPENSE),
                Category(name = "Rent", color = 0xFFFF5722.toInt(), icon = "🏘️", type = CategoryType.EXPENSE),
                Category(name = "Mortgage", color = 0xFF795548.toInt(), icon = "🏠", type = CategoryType.EXPENSE),
                Category(name = "Home Maintenance", color = 0xFF8D6E63.toInt(), icon = "🔨", type = CategoryType.EXPENSE),
                Category(name = "Furniture", color = 0xFF795548.toInt(), icon = "🪑", type = CategoryType.EXPENSE),
                
                // Health & Wellness
                Category(name = "Healthcare", color = 0xFFF06292.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Doctor Visits", color = 0xFFE91E63.toInt(), icon = "👨‍⚕️", type = CategoryType.EXPENSE),
                Category(name = "Medications", color = 0xFF9C27B0.toInt(), icon = "💊", type = CategoryType.EXPENSE),
                Category(name = "Dental Care", color = 0xFFE0E0E0.toInt(), icon = "🦷", type = CategoryType.EXPENSE),
                Category(name = "Vision Care", color = 0xFF2196F3.toInt(), icon = "👓", type = CategoryType.EXPENSE),
                Category(name = "Gym Membership", color = 0xFF4CAF50.toInt(), icon = "💪", type = CategoryType.EXPENSE),
                Category(name = "Personal Care", color = 0xFFF8BBD9.toInt(), icon = "💄", type = CategoryType.EXPENSE),
                Category(name = "Beauty & Cosmetics", color = 0xFFE91E63.toInt(), icon = "💅", type = CategoryType.EXPENSE),
                
                // Education & Development
                Category(name = "Education", color = 0xFF4FC3F7.toInt(), icon = "📚", type = CategoryType.EXPENSE),
                Category(name = "Tuition", color = 0xFF2196F3.toInt(), icon = "🎓", type = CategoryType.EXPENSE),
                Category(name = "Books & Courses", color = 0xFF795548.toInt(), icon = "📖", type = CategoryType.EXPENSE),
                Category(name = "Online Learning", color = 0xFF3F51B5.toInt(), icon = "💻", type = CategoryType.EXPENSE),
                Category(name = "Workshops", color = 0xFFFF9800.toInt(), icon = "🎯", type = CategoryType.EXPENSE),
                
                // Financial & Business
                Category(name = "Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Health Insurance", color = 0xFF4CAF50.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Car Insurance", color = 0xFF2196F3.toInt(), icon = "🚗", type = CategoryType.EXPENSE),
                Category(name = "Life Insurance", color = 0xFF9C27B0.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Taxes", color = 0xFF4DB6AC.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                Category(name = "Bank Fees", color = 0xFF795548.toInt(), icon = "🏦", type = CategoryType.EXPENSE),
                Category(name = "Credit Card Fees", color = 0xFFE91E63.toInt(), icon = "💳", type = CategoryType.EXPENSE),
                Category(name = "Investments", color = 0xFF4CAF50.toInt(), icon = "📈", type = CategoryType.EXPENSE),
                Category(name = "Stock Trading", color = 0xFF4CAF50.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                Category(name = "Crypto", color = 0xFFFF9800.toInt(), icon = "₿", type = CategoryType.EXPENSE),
                
                // Modern Lifestyle
                Category(name = "Subscriptions", color = 0xFF90A4AE.toInt(), icon = "📱", type = CategoryType.EXPENSE),
                Category(name = "Streaming Services", color = 0xFFE91E63.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Music Services", color = 0xFF9C27B0.toInt(), icon = "🎵", type = CategoryType.EXPENSE),
                Category(name = "Cloud Storage", color = 0xFF2196F3.toInt(), icon = "☁️", type = CategoryType.EXPENSE),
                Category(name = "Software Licenses", color = 0xFF3F51B5.toInt(), icon = "💻", type = CategoryType.EXPENSE),
                
                // Social & Gifts
                Category(name = "Gifts", color = 0xFFFFB74D.toInt(), icon = "🎁", type = CategoryType.EXPENSE),
                Category(name = "Birthday Gifts", color = 0xFFE91E63.toInt(), icon = "🎂", type = CategoryType.EXPENSE),
                Category(name = "Holiday Gifts", color = 0xFFFF5722.toInt(), icon = "🎄", type = CategoryType.EXPENSE),
                Category(name = "Charity", color = 0xFF4CAF50.toInt(), icon = "🤝", type = CategoryType.EXPENSE),
                Category(name = "Tips", color = 0xFFFF9800.toInt(), icon = "💡", type = CategoryType.EXPENSE),
                
                // Travel & Recreation
                Category(name = "Travel", color = 0xFF81C784.toInt(), icon = "✈️", type = CategoryType.EXPENSE),
                Category(name = "Airfare", color = 0xFF2196F3.toInt(), icon = "✈️", type = CategoryType.EXPENSE),
                Category(name = "Hotels", color = 0xFF9C27B0.toInt(), icon = "🏨", type = CategoryType.EXPENSE),
                Category(name = "Vacation Activities", color = 0xFFFF9800.toInt(), icon = "🏖️", type = CategoryType.EXPENSE),
                Category(name = "Local Outings", color = 0xFF4CAF50.toInt(), icon = "🚶", type = CategoryType.EXPENSE),
                
                // Work & Professional
                Category(name = "Work Expenses", color = 0xFF607D8B.toInt(), icon = "💼", type = CategoryType.EXPENSE),
                Category(name = "Office Supplies", color = 0xFF795548.toInt(), icon = "📎", type = CategoryType.EXPENSE),
                Category(name = "Professional Development", color = 0xFF2196F3.toInt(), icon = "🎯", type = CategoryType.EXPENSE),
                Category(name = "Business Meals", color = 0xFFE57373.toInt(), icon = "🍽️", type = CategoryType.EXPENSE),
                
                // Miscellaneous
                Category(name = "Pet Expenses", color = 0xFF8D6E63.toInt(), icon = "🐕", type = CategoryType.EXPENSE),
                Category(name = "Legal Fees", color = 0xFF795548.toInt(), icon = "⚖️", type = CategoryType.EXPENSE),
                Category(name = "Repairs", color = 0xFF424242.toInt(), icon = "🔧", type = CategoryType.EXPENSE),
                Category(name = "Emergency Fund", color = 0xFFFF5722.toInt(), icon = "🚨", type = CategoryType.EXPENSE),
                
                // Essential Daily Expenses
                Category(name = "Gas & Fuel", color = 0xFF795548.toInt(), icon = "⛽", type = CategoryType.EXPENSE),
                Category(name = "Public Transport", color = 0xFF607D8B.toInt(), icon = "🚌", type = CategoryType.EXPENSE),
                Category(name = "Uber/Lyft", color = 0xFF9C27B0.toInt(), icon = "🚕", type = CategoryType.EXPENSE),
                Category(name = "Parking Fees", color = 0xFF795548.toInt(), icon = "🅿️", type = CategoryType.EXPENSE),
                Category(name = "Car Insurance", color = 0xFF2196F3.toInt(), icon = "🚗", type = CategoryType.EXPENSE),
                Category(name = "Car Payment", color = 0xFF4CAF50.toInt(), icon = "🚙", type = CategoryType.EXPENSE),
                Category(name = "Auto Maintenance", color = 0xFF424242.toInt(), icon = "🔧", type = CategoryType.EXPENSE),
                
                // Food & Dining - More Specific
                Category(name = "Restaurants", color = 0xFFE57373.toInt(), icon = "🍕", type = CategoryType.EXPENSE),
                Category(name = "Fast Food", color = 0xFFFF7043.toInt(), icon = "🍔", type = CategoryType.EXPENSE),
                Category(name = "Coffee Shops", color = 0xFF8D6E63.toInt(), icon = "☕", type = CategoryType.EXPENSE),
                Category(name = "Bars & Clubs", color = 0xFF9C27B0.toInt(), icon = "🍺", type = CategoryType.EXPENSE),
                Category(name = "Food Delivery", color = 0xFFFF9800.toInt(), icon = "🛵", type = CategoryType.EXPENSE),
                
                // Shopping - Essential Categories
                Category(name = "Clothing & Shoes", color = 0xFFE1BEE7.toInt(), icon = "👕", type = CategoryType.EXPENSE),
                Category(name = "Electronics", color = 0xFF90A4AE.toInt(), icon = "📱", type = CategoryType.EXPENSE),
                Category(name = "Home & Furniture", color = 0xFF8BC34A.toInt(), icon = "🏡", type = CategoryType.EXPENSE),
                Category(name = "Books & Media", color = 0xFF795548.toInt(), icon = "📚", type = CategoryType.EXPENSE),
                Category(name = "Sports Equipment", color = 0xFF4CAF50.toInt(), icon = "⚽", type = CategoryType.EXPENSE),
                Category(name = "Beauty Products", color = 0xFFE91E63.toInt(), icon = "💄", type = CategoryType.EXPENSE),
                Category(name = "Jewelry", color = 0xFFFFD700.toInt(), icon = "💍", type = CategoryType.EXPENSE),
                
                // Entertainment & Leisure
                Category(name = "Movies & TV Shows", color = 0xFFE91E63.toInt(), icon = "🎭", type = CategoryType.EXPENSE),
                Category(name = "Video Games", color = 0xFF9C27B0.toInt(), icon = "🎮", type = CategoryType.EXPENSE),
                Category(name = "Concerts & Events", color = 0xFFE91E63.toInt(), icon = "🎵", type = CategoryType.EXPENSE),
                Category(name = "Sports Events", color = 0xFF4CAF50.toInt(), icon = "⚽", type = CategoryType.EXPENSE),
                Category(name = "Hobbies & Crafts", color = 0xFFFF9800.toInt(), icon = "🎨", type = CategoryType.EXPENSE),
                Category(name = "Gym & Fitness", color = 0xFF4CAF50.toInt(), icon = "💪", type = CategoryType.EXPENSE),
                Category(name = "Outdoor Activities", color = 0xFF8BC34A.toInt(), icon = "🏃", type = CategoryType.EXPENSE),
                
                // Home & Utilities - Essential
                Category(name = "Rent", color = 0xFFFF5722.toInt(), icon = "🏘️", type = CategoryType.EXPENSE),
                Category(name = "Mortgage", color = 0xFF795548.toInt(), icon = "🏠", type = CategoryType.EXPENSE),
                Category(name = "Electricity", color = 0xFFFFEB3B.toInt(), icon = "💡", type = CategoryType.EXPENSE),
                Category(name = "Water & Sewer", color = 0xFF2196F3.toInt(), icon = "💧", type = CategoryType.EXPENSE),
                Category(name = "Internet", color = 0xFF3F51B5.toInt(), icon = "🌐", type = CategoryType.EXPENSE),
                Category(name = "Phone Bill", color = 0xFF9C27B0.toInt(), icon = "📞", type = CategoryType.EXPENSE),
                Category(name = "Cable TV", color = 0xFFE91E63.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Home Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Property Tax", color = 0xFF4DB6AC.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                Category(name = "Home Maintenance", color = 0xFF8D6E63.toInt(), icon = "🔨", type = CategoryType.EXPENSE),
                Category(name = "Cleaning Supplies", color = 0xFFE0E0E0.toInt(), icon = "🧹", type = CategoryType.EXPENSE),
                
                // Health & Wellness - Essential
                Category(name = "Doctor Visits", color = 0xFFE91E63.toInt(), icon = "👨‍⚕️", type = CategoryType.EXPENSE),
                Category(name = "Dentist", color = 0xFFE0E0E0.toInt(), icon = "🦷", type = CategoryType.EXPENSE),
                Category(name = "Eye Doctor", color = 0xFF2196F3.toInt(), icon = "👓", type = CategoryType.EXPENSE),
                Category(name = "Prescriptions", color = 0xFF9C27B0.toInt(), icon = "💊", type = CategoryType.EXPENSE),
                Category(name = "Health Insurance", color = 0xFF4CAF50.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Dental Insurance", color = 0xFFE0E0E0.toInt(), icon = "🦷", type = CategoryType.EXPENSE),
                Category(name = "Vision Insurance", color = 0xFF2196F3.toInt(), icon = "👓", type = CategoryType.EXPENSE),
                Category(name = "Mental Health", color = 0xFF9C27B0.toInt(), icon = "🧠", type = CategoryType.EXPENSE),
                Category(name = "Physical Therapy", color = 0xFF4CAF50.toInt(), icon = "💪", type = CategoryType.EXPENSE),
                
                // Education & Development
                Category(name = "College Tuition", color = 0xFF2196F3.toInt(), icon = "🎓", type = CategoryType.EXPENSE),
                Category(name = "Student Loans", color = 0xFF4DB6AC.toInt(), icon = "📚", type = CategoryType.EXPENSE),
                Category(name = "Online Courses", color = 0xFF3F51B5.toInt(), icon = "💻", type = CategoryType.EXPENSE),
                Category(name = "Books & Materials", color = 0xFF795548.toInt(), icon = "📖", type = CategoryType.EXPENSE),
                Category(name = "Workshops", color = 0xFFFF9800.toInt(), icon = "🎯", type = CategoryType.EXPENSE),
                Category(name = "Certifications", color = 0xFF4CAF50.toInt(), icon = "🏆", type = CategoryType.EXPENSE),
                
                // Financial & Business
                Category(name = "Life Insurance", color = 0xFF9C27B0.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Disability Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Long-term Care", color = 0xFF4DB6AC.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Income Tax", color = 0xFF4DB6AC.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                Category(name = "Property Tax", color = 0xFF4DB6AC.toInt(), icon = "🏠", type = CategoryType.EXPENSE),
                Category(name = "Sales Tax", color = 0xFF4DB6AC.toInt(), icon = "🛒", type = CategoryType.EXPENSE),
                Category(name = "Bank Fees", color = 0xFF795548.toInt(), icon = "🏦", type = CategoryType.EXPENSE),
                Category(name = "ATM Fees", color = 0xFF795548.toInt(), icon = "🏧", type = CategoryType.EXPENSE),
                Category(name = "Credit Card Fees", color = 0xFFE91E63.toInt(), icon = "💳", type = CategoryType.EXPENSE),
                Category(name = "Investment Fees", color = 0xFF4CAF50.toInt(), icon = "📈", type = CategoryType.EXPENSE),
                Category(name = "Crypto Trading", color = 0xFFFF9800.toInt(), icon = "₿", type = CategoryType.EXPENSE),
                Category(name = "Stock Trading", color = 0xFF4CAF50.toInt(), icon = "📊", type = CategoryType.EXPENSE),
                
                // Modern Lifestyle - Essential
                Category(name = "Netflix", color = 0xFFE91E63.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Spotify", color = 0xFF4CAF50.toInt(), icon = "🎵", type = CategoryType.EXPENSE),
                Category(name = "Amazon Prime", color = 0xFFFF9800.toInt(), icon = "📦", type = CategoryType.EXPENSE),
                Category(name = "YouTube Premium", color = 0xFFFF0000.toInt(), icon = "📺", type = CategoryType.EXPENSE),
                Category(name = "Apple Services", color = 0xFF000000.toInt(), icon = "🍎", type = CategoryType.EXPENSE),
                Category(name = "Google Services", color = 0xFF4285F4.toInt(), icon = "🔍", type = CategoryType.EXPENSE),
                Category(name = "Cloud Storage", color = 0xFF2196F3.toInt(), icon = "☁️", type = CategoryType.EXPENSE),
                Category(name = "Software Subscriptions", color = 0xFF3F51B5.toInt(), icon = "💻", type = CategoryType.EXPENSE),
                
                // Social & Gifts - Essential
                Category(name = "Birthday Gifts", color = 0xFFE91E63.toInt(), icon = "🎂", type = CategoryType.EXPENSE),
                Category(name = "Holiday Gifts", color = 0xFFFF5722.toInt(), icon = "🎄", type = CategoryType.EXPENSE),
                Category(name = "Wedding Gifts", color = 0xFFE1BEE7.toInt(), icon = "💒", type = CategoryType.EXPENSE),
                Category(name = "Charity Donations", color = 0xFF4CAF50.toInt(), icon = "🤝", type = CategoryType.EXPENSE),
                Category(name = "Tips & Gratuity", color = 0xFFFF9800.toInt(), icon = "💡", type = CategoryType.EXPENSE),
                Category(name = "Party Expenses", color = 0xFFE91E63.toInt(), icon = "🎉", type = CategoryType.EXPENSE),
                
                // Travel & Recreation - Essential
                Category(name = "Airplane Tickets", color = 0xFF2196F3.toInt(), icon = "✈️", type = CategoryType.EXPENSE),
                Category(name = "Hotel Stays", color = 0xFF9C27B0.toInt(), icon = "🏨", type = CategoryType.EXPENSE),
                Category(name = "Car Rentals", color = 0xFF4CAF50.toInt(), icon = "🚗", type = CategoryType.EXPENSE),
                Category(name = "Vacation Activities", color = 0xFFFF9800.toInt(), icon = "🏖️", type = CategoryType.EXPENSE),
                Category(name = "Local Outings", color = 0xFF4CAF50.toInt(), icon = "🚶", type = CategoryType.EXPENSE),
                Category(name = "Sightseeing", color = 0xFF81C784.toInt(), icon = "🗺️", type = CategoryType.EXPENSE),
                Category(name = "Travel Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                
                // Work & Professional - Essential
                Category(name = "Office Supplies", color = 0xFF795548.toInt(), icon = "📎", type = CategoryType.EXPENSE),
                Category(name = "Business Meals", color = 0xFFE57373.toInt(), icon = "🍽️", type = CategoryType.EXPENSE),
                Category(name = "Professional Development", color = 0xFF2196F3.toInt(), icon = "🎯", type = CategoryType.EXPENSE),
                Category(name = "Conference Fees", color = 0xFF4CAF50.toInt(), icon = "🎤", type = CategoryType.EXPENSE),
                Category(name = "Work Equipment", color = 0xFF90A4AE.toInt(), icon = "💻", type = CategoryType.EXPENSE),
                Category(name = "Business Travel", color = 0xFF81C784.toInt(), icon = "✈️", type = CategoryType.EXPENSE),
                Category(name = "Work Clothing", color = 0xFFE1BEE7.toInt(), icon = "👔", type = CategoryType.EXPENSE),
                
                // Pet Care - Essential
                Category(name = "Pet Food", color = 0xFF8D6E63.toInt(), icon = "🐕", type = CategoryType.EXPENSE),
                Category(name = "Veterinary Care", color = 0xFFF06292.toInt(), icon = "🏥", type = CategoryType.EXPENSE),
                Category(name = "Pet Insurance", color = 0xFF9575CD.toInt(), icon = "🛡️", type = CategoryType.EXPENSE),
                Category(name = "Pet Supplies", color = 0xFF8D6E63.toInt(), icon = "🐾", type = CategoryType.EXPENSE),
                Category(name = "Pet Grooming", color = 0xFFE1BEE7.toInt(), icon = "✂️", type = CategoryType.EXPENSE),
                
                // Miscellaneous - Essential
                Category(name = "Legal Services", color = 0xFF795548.toInt(), icon = "⚖️", type = CategoryType.EXPENSE),
                Category(name = "Home Repairs", color = 0xFF424242.toInt(), icon = "🔧", type = CategoryType.EXPENSE),
                Category(name = "Emergency Expenses", color = 0xFFFF5722.toInt(), icon = "🚨", type = CategoryType.EXPENSE),
                Category(name = "Moving Expenses", color = 0xFF607D8B.toInt(), icon = "📦", type = CategoryType.EXPENSE),
                Category(name = "Storage Fees", color = 0xFF795548.toInt(), icon = "📦", type = CategoryType.EXPENSE),
                Category(name = "Laundry", color = 0xFFE0E0E0.toInt(), icon = "👕", type = CategoryType.EXPENSE),
                Category(name = "Haircuts", color = 0xFFE1BEE7.toInt(), icon = "✂️", type = CategoryType.EXPENSE),
                Category(name = "Nail Care", color = 0xFFE91E63.toInt(), icon = "💅", type = CategoryType.EXPENSE),
                
                // Income Categories - MOST IMPORTANT FIRST
                Category(name = "Salary", color = 0xFF4DB6AC.toInt(), icon = "💰", type = CategoryType.INCOME),
                Category(name = "Hourly Wages", color = 0xFF4CAF50.toInt(), icon = "⏰", type = CategoryType.INCOME),
                Category(name = "Freelance", color = 0xFF81C784.toInt(), icon = "💼", type = CategoryType.INCOME),
                Category(name = "Business Income", color = 0xFF4CAF50.toInt(), icon = "🏢", type = CategoryType.INCOME),
                Category(name = "Investment Returns", color = 0xFF4DB6AC.toInt(), icon = "📈", type = CategoryType.INCOME),
                Category(name = "Rental Income", color = 0xFF81C784.toInt(), icon = "🏠", type = CategoryType.INCOME),
                Category(name = "Side Hustle", color = 0xFFFFB74D.toInt(), icon = "🚀", type = CategoryType.INCOME),
                Category(name = "Bonus", color = 0xFF4CAF50.toInt(), icon = "🎯", type = CategoryType.INCOME),
                Category(name = "Performance Bonus", color = 0xFF4CAF50.toInt(), icon = "🏆", type = CategoryType.INCOME),
                Category(name = "Tips", color = 0xFFFF9800.toInt(), icon = "💡", type = CategoryType.INCOME),
                Category(name = "Commission", color = 0xFFE91E63.toInt(), icon = "💼", type = CategoryType.INCOME),
                Category(name = "Overtime Pay", color = 0xFFFF9800.toInt(), icon = "🕐", type = CategoryType.INCOME),
                Category(name = "Signing Bonus", color = 0xFF2196F3.toInt(), icon = "✍️", type = CategoryType.INCOME),
                Category(name = "Annual Bonus", color = 0xFF4CAF50.toInt(), icon = "🎯", type = CategoryType.INCOME),
                
                // Freelance & Business
                Category(name = "Freelance", color = 0xFF81C784.toInt(), icon = "💼", type = CategoryType.INCOME),
                Category(name = "Business Income", color = 0xFF4CAF50.toInt(), icon = "🏢", type = CategoryType.INCOME),
                Category(name = "Consulting", color = 0xFF2196F3.toInt(), icon = "💡", type = CategoryType.INCOME),
                Category(name = "Online Business", color = 0xFF9C27B0.toInt(), icon = "🌐", type = CategoryType.INCOME),
                Category(name = "Side Hustle", color = 0xFFFFB74D.toInt(), icon = "🚀", type = CategoryType.INCOME),
                Category(name = "Gig Economy", color = 0xFF607D8B.toInt(), icon = "🚗", type = CategoryType.INCOME),
                
                // Investment & Returns
                Category(name = "Investment Returns", color = 0xFF4DB6AC.toInt(), icon = "📈", type = CategoryType.INCOME),
                Category(name = "Stock Dividends", color = 0xFF4CAF50.toInt(), icon = "📊", type = CategoryType.INCOME),
                Category(name = "Interest Income", color = 0xFF2196F3.toInt(), icon = "🏦", type = CategoryType.INCOME),
                Category(name = "Crypto Gains", color = 0xFFFF9800.toInt(), icon = "₿", type = CategoryType.INCOME),
                Category(name = "Real Estate Income", color = 0xFF8D6E63.toInt(), icon = "🏠", type = CategoryType.INCOME),
                
                // Property & Assets
                Category(name = "Rental Income", color = 0xFF81C784.toInt(), icon = "🏠", type = CategoryType.INCOME),
                Category(name = "Property Sale", color = 0xFF795548.toInt(), icon = "🏘️", type = CategoryType.INCOME),
                Category(name = "Asset Sales", color = 0xFF607D8B.toInt(), icon = "💎", type = CategoryType.INCOME),
                
                // Bonuses & Rewards
                Category(name = "Bonus", color = 0xFF4CAF50.toInt(), icon = "🎯", type = CategoryType.INCOME),
                Category(name = "Performance Bonus", color = 0xFF4CAF50.toInt(), icon = "🏆", type = CategoryType.INCOME),
                Category(name = "Signing Bonus", color = 0xFF2196F3.toInt(), icon = "✍️", type = CategoryType.INCOME),
                Category(name = "Cashback Rewards", color = 0xFF4CAF50.toInt(), icon = "💳", type = CategoryType.INCOME),
                Category(name = "Credit Card Rewards", color = 0xFF9C27B0.toInt(), icon = "🎁", type = CategoryType.INCOME),
                Category(name = "Loyalty Points", color = 0xFFFF9800.toInt(), icon = "⭐", type = CategoryType.INCOME),
                
                // Refunds & Reimbursements
                Category(name = "Refunds", color = 0xFF4DB6AC.toInt(), icon = "↩️", type = CategoryType.INCOME),
                Category(name = "Tax Refunds", color = 0xFF4DB6AC.toInt(), icon = "📊", type = CategoryType.INCOME),
                Category(name = "Insurance Claims", color = 0xFF4CAF50.toInt(), icon = "🛡️", type = CategoryType.INCOME),
                Category(name = "Expense Reimbursement", color = 0xFF2196F3.toInt(), icon = "💼", type = CategoryType.INCOME),
                Category(name = "Warranty Claims", color = 0xFF9C27B0.toInt(), icon = "🔧", type = CategoryType.INCOME),
                
                // Other Income Sources
                Category(name = "Gambling Winnings", color = 0xFFFF9800.toInt(), icon = "🎰", type = CategoryType.INCOME),
                Category(name = "Lottery Winnings", color = 0xFFE91E63.toInt(), icon = "🎯", type = CategoryType.INCOME),
                Category(name = "Inheritance", color = 0xFF795548.toInt(), icon = "💝", type = CategoryType.INCOME),
                Category(name = "Gifts Received", color = 0xFFFFB74D.toInt(), icon = "🎁", type = CategoryType.INCOME),
                Category(name = "Settlement", color = 0xFF607D8B.toInt(), icon = "⚖️", type = CategoryType.INCOME),

                Category(name = "Child Support", color = 0xFF4CAF50.toInt(), icon = "👶", type = CategoryType.INCOME),
                
                // Transfer Category
                Category(name = "Transfer", color = 0xFF9E9E9E.toInt(), icon = "↔️", type = CategoryType.TRANSFER)
            )

            defaultCategories.forEach { category ->
                val values = ContentValues().apply {
                    put("name", category.name)
                    put("color", category.color)
                    put("icon", category.icon)
                    put("type", category.type.name)
                    put("is_default", if (category.isDefault) 1 else 0)
                    put("usage_count", category.usageCount)
                    put("created_at", LocalDateTime.now().toString())
                    put("updated_at", LocalDateTime.now().toString())
                }
                val categoryId = db.insert("categories", null, values)
                Log.d(TAG, "Inserted category: ${category.name} with ID: $categoryId")
            }

            // Insert default account
            val defaultAccount = Account(
                name = "Cash",
                type = AccountType.CASH,
                balance = BigDecimal("1000.00"),
                color = 0xFF4CAF50.toInt(),
                icon = "💵"
            )

            val accountValues = ContentValues().apply {
                put("name", defaultAccount.name)
                put("type", defaultAccount.type.name)
                put("balance", defaultAccount.balance.toString())
                put("color", defaultAccount.color)
                put("icon", defaultAccount.icon)
                put("is_active", 1)
                put("created_at", LocalDateTime.now().toString())
                put("updated_at", LocalDateTime.now().toString())
            }
            val accountId = db.insert("accounts", null, accountValues)
            Log.d(TAG, "Inserted default account: ${defaultAccount.name} with ID: $accountId")
            
            Log.d(TAG, "Default data inserted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting default data", e)
            throw e
        }
    }

    // Helper methods for type conversion
    fun localDateTimeToString(dateTime: LocalDateTime): String = dateTime.toString()
    fun stringToLocalDateTime(dateTimeString: String): LocalDateTime = LocalDateTime.parse(dateTimeString)
    fun bigDecimalToString(bigDecimal: BigDecimal): String = bigDecimal.toString()
    fun stringToBigDecimal(bigDecimalString: String): BigDecimal = BigDecimal(bigDecimalString)
    fun yearMonthToString(yearMonth: YearMonth): String = yearMonth.toString()
    fun stringToYearMonth(yearMonthString: String): YearMonth = YearMonth.parse(yearMonthString)
    
    // Debug method to check database state
    fun checkDatabaseState(): Boolean {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
            val tables = mutableListOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    tables.add(it.getString(0))
                }
            }
            Log.d(TAG, "Database tables: $tables")
            
            // Check if required tables exist
            val requiredTables = listOf("transactions", "accounts", "categories")
            val allTablesExist = requiredTables.all { tableName ->
                tables.contains(tableName)
            }
            
            if (allTablesExist) {
                // Check if tables have data
                val transactionCount = db.rawQuery("SELECT COUNT(*) FROM transactions", null).use { 
                    if (it.moveToFirst()) it.getInt(0) else 0 
                }
                val accountCount = db.rawQuery("SELECT COUNT(*) FROM accounts", null).use { 
                    if (it.moveToFirst()) it.getInt(0) else 0 
                }
                val categoryCount = db.rawQuery("SELECT COUNT(*) FROM categories", null).use { 
                    if (it.moveToFirst()) it.getInt(0) else 0 
                }
                
                Log.d(TAG, "Table counts - Transactions: $transactionCount, Accounts: $accountCount, Categories: $categoryCount")
            }
            
            allTablesExist
        } catch (e: Exception) {
            Log.e(TAG, "Error checking database state", e)
            false
        }
    }
}
