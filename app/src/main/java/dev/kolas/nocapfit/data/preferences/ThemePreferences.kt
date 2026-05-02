package dev.kolas.nocapfit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { LIGHT, DARK, SYSTEM }

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val notificationSoundUriKey = stringPreferencesKey("notification_sound_uri")
    private val completionSoundUriKey = stringPreferencesKey("completion_sound_uri")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val name = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    val notificationSoundUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[notificationSoundUriKey]
    }

    val completionSoundUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[completionSoundUriKey]
    }

    val dynamicColor: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[dynamicColorKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[dynamicColorKey] = enabled
        }
    }

    suspend fun setNotificationSoundUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[notificationSoundUriKey] = uri
            } else {
                preferences.remove(notificationSoundUriKey)
            }
        }
    }

    suspend fun setCompletionSoundUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[completionSoundUriKey] = uri
            } else {
                preferences.remove(completionSoundUriKey)
            }
        }
    }
}
