package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tripbudget_prefs"
)

data class UserPreferences(
    val currency: AppCurrency = AppCurrency.USD,
    val isDarkMode: Boolean = false,
    val activeTripId: Long = -1L
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val ACTIVE_TRIP_ID = longPreferencesKey("active_trip_id")
    }

    val preferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            currency = AppCurrency.fromCode(prefs[Keys.CURRENCY_CODE]),
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            activeTripId = prefs[Keys.ACTIVE_TRIP_ID] ?: -1L
        )
    }

    suspend fun setCurrency(currency: AppCurrency) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_CODE] = currency.code
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setActiveTripId(tripId: Long) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_TRIP_ID] = tripId
        }
    }
}
