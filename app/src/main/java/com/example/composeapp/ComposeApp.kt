package com.example.composeapp

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.example.composeapp.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ComposeApp : MultiDexApplication() {
    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
    }
}
