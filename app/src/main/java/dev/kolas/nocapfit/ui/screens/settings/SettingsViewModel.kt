package dev.kolas.nocapfit.ui.screens.settings

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kolas.nocapfit.data.backup.BackupManager
import dev.kolas.nocapfit.data.preferences.ThemeMode
import dev.kolas.nocapfit.data.preferences.ThemePreferences
import dev.kolas.nocapfit.util.SOUND_URI_SILENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupEvent {
    data object Idle : BackupEvent
    data object Exporting : BackupEvent
    data object Importing : BackupEvent
    data class Success(val message: String) : BackupEvent
    data class Error(val message: String) : BackupEvent
    data object RestartRequired : BackupEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val backupManager: BackupManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val dynamicColor: StateFlow<Boolean> = themePreferences.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationSoundUri: StateFlow<String?> = themePreferences.notificationSoundUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val completionSoundUri: StateFlow<String?> = themePreferences.completionSoundUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Ringtone title lookup is a binder/disk call — resolve off the main thread, not in composition.
    val notificationSoundTitle: StateFlow<String> = themePreferences.notificationSoundUri
        .map(::resolveSoundTitle)
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val completionSoundTitle: StateFlow<String> = themePreferences.completionSoundUri
        .map(::resolveSoundTitle)
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private fun resolveSoundTitle(soundUri: String?): String = when (soundUri) {
        null -> "Default"
        SOUND_URI_SILENT -> "Silent"
        else -> try {
            RingtoneManager.getRingtone(context, soundUri.toUri())?.getTitle(context) ?: "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private val _backupEvent = MutableStateFlow<BackupEvent>(BackupEvent.Idle)
    val backupEvent: StateFlow<BackupEvent> = _backupEvent.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }

    fun setNotificationSoundUri(uri: String?) {
        viewModelScope.launch {
            themePreferences.setNotificationSoundUri(uri)
        }
    }

    fun setCompletionSoundUri(uri: String?) {
        viewModelScope.launch {
            themePreferences.setCompletionSoundUri(uri)
        }
    }

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _backupEvent.value = BackupEvent.Exporting
            backupManager.exportDatabase(uri).fold(
                onSuccess = { _backupEvent.value = BackupEvent.Success("Backup exported") },
                onFailure = { _backupEvent.value = BackupEvent.Error(it.message ?: "Export failed") }
            )
        }
    }

    private var pendingBackupBytes: ByteArray? = null

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _backupEvent.value = BackupEvent.Importing
            backupManager.validateBackup(uri).fold(
                onSuccess = {
                    pendingBackupBytes = it
                    _backupEvent.value = BackupEvent.RestartRequired
                },
                onFailure = { _backupEvent.value = BackupEvent.Error(it.message ?: "Import failed") }
            )
        }
    }

    suspend fun applyPendingBackup() {
        val bytes = pendingBackupBytes ?: return
        backupManager.applyBackup(bytes)
    }

    suspend fun hasActiveWorkout(): Boolean = backupManager.hasActiveWorkout()

    fun clearBackupEvent() {
        _backupEvent.value = BackupEvent.Idle
    }
}
