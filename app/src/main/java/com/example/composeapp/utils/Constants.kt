package com.example.composeapp.utils

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    object SessionPrefs {
        const val DATASTORE_NAME = "user_session"
        val USER_ID = intPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
    }

}