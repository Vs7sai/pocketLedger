package com.v7techsolution.pocketledger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PocketLedgerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
