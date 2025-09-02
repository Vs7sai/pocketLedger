package com.v7techsolution.pocketledger.di

import android.content.Context
import com.v7techsolution.pocketledger.data.database.SQLiteDatabaseHelper
import com.v7techsolution.pocketledger.data.manager.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSQLiteDatabaseHelper(@ApplicationContext context: Context): SQLiteDatabaseHelper {
        return SQLiteDatabaseHelper(context)
    }

    @Provides
    @Singleton
    fun provideTransactionManager(dbHelper: SQLiteDatabaseHelper): TransactionManager {
        return TransactionManager(dbHelper)
    }

    @Provides
    @Singleton
    fun provideAccountManager(dbHelper: SQLiteDatabaseHelper): AccountManager {
        return AccountManager(dbHelper)
    }

    @Provides
    @Singleton
    fun provideCategoryManager(dbHelper: SQLiteDatabaseHelper): CategoryManager {
        return CategoryManager(dbHelper)
    }

    @Provides
    @Singleton
    fun provideBudgetManager(dbHelper: SQLiteDatabaseHelper): BudgetManager {
        return BudgetManager(dbHelper)
    }

    @Provides
    @Singleton
    fun providePaymentReminderManager(dbHelper: SQLiteDatabaseHelper): PaymentReminderManager {
        return PaymentReminderManager(dbHelper)
    }
}
