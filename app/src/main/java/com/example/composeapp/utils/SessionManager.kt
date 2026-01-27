package com.example.composeapp.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.composeapp.validation.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val Context.dataStore by preferencesDataStore(
        name = Constants.SessionPrefs.DATASTORE_NAME
    )

    private val dataStore = context.dataStore

    suspend fun saveUser(userId: Int, username: String) {
        dataStore.edit { prefs ->
            prefs[Constants.SessionPrefs.USER_ID] = userId
            prefs[Constants.SessionPrefs.USERNAME] = username
        }
    }

    val userFlow: Flow<UserSession?> =
        dataStore.data.map { prefs ->
            val id = prefs[Constants.SessionPrefs.USER_ID] ?: return@map null
            val name = prefs[Constants.SessionPrefs.USERNAME] ?: return@map null
            UserSession(id, name)
        }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
