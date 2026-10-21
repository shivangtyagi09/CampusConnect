package com.example.campusconnect

import android.app.Application
import com.example.campusconnect.data.local.AppDatabase
import com.example.campusconnect.data.remote.FirebaseSyncManager
import com.google.firebase.FirebaseApp

class CampusConnectApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    companion object {
        lateinit var instance: CampusConnectApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            FirebaseApp.initializeApp(this)
            FirebaseSyncManager.startSync(this)
        } catch (e: Exception) {
            // Keep app functional even if Google Play Services is delayed
        }
    }
}
