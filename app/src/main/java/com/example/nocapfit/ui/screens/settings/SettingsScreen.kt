package com.example.nocapfit.ui.screens.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.BuildConfig
import com.example.nocapfit.data.preferences.ThemeMode
import com.example.nocapfit.ui.components.ConfirmDialog
import com.example.nocapfit.ui.components.SectionHeader
import com.example.nocapfit.util.NOTIFICATION_SOUND_SILENT
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val backupEvent by viewModel.backupEvent.collectAsState()
    val notificationSoundUri by viewModel.notificationSoundUri.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var importUri by remember { mutableStateOf<Uri?>(null) }
    var hasActiveWorkout by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) viewModel.exportDatabase(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            importUri = uri
            scope.launch {
                hasActiveWorkout = viewModel.hasActiveWorkout()
            }
        }
    }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val pickedUri = result.data
                ?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            viewModel.setNotificationSoundUri(pickedUri?.toString() ?: NOTIFICATION_SOUND_SILENT)
        }
    }

    HandleBackupSnackbar(backupEvent, snackbarHostState, viewModel::clearBackupEvent)
    BackupDialogs(
        importUri = importUri,
        hasActiveWorkout = hasActiveWorkout,
        backupEvent = backupEvent,
        onImportConfirm = { uri ->
            importUri = null
            viewModel.importDatabase(uri)
        },
        onImportDismiss = { importUri = null },
        onApplyAndRestart = { viewModel.applyPendingBackup() }
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        SettingsContent(
            themeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            dynamicColor = dynamicColor,
            onDynamicColorChange = { viewModel.setDynamicColor(it) },
            notificationSoundUri = notificationSoundUri,
            onTimerSoundClick = {
                ringtoneLauncher.launch(createRingtonePickerIntent(notificationSoundUri))
            },
            isBackupInProgress = backupEvent is BackupEvent.Exporting ||
                backupEvent is BackupEvent.Importing,
            onExportClick = { exportLauncher.launch(generateBackupFileName()) },
            onImportClick = {
                importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            },
            versionName = BuildConfig.VERSION_NAME,
            osVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT_FULL})",
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun HandleBackupSnackbar(
    backupEvent: BackupEvent,
    snackbarHostState: SnackbarHostState,
    onClear: () -> Unit
) {
    LaunchedEffect(backupEvent) {
        when (backupEvent) {
            is BackupEvent.Success -> {
                snackbarHostState.showSnackbar(backupEvent.message)
                onClear()
            }
            is BackupEvent.Error -> {
                snackbarHostState.showSnackbar(backupEvent.message)
                onClear()
            }
            else -> {}
        }
    }
}

@Composable
private fun ImportConfirmDialog(
    hasActiveWorkout: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val warningText = buildString {
        append("This will replace all current data with the backup. This cannot be undone.")
        if (hasActiveWorkout) {
            append("\n\nYou have an active workout that will be lost.")
        }
    }
    ConfirmDialog(
        title = "Import Data",
        message = warningText,
        confirmLabel = "Import",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun RestartDialog(context: android.content.Context, onApplyAndRestart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Data Restored") },
        text = { Text("Data restored successfully. The app will restart.") },
        confirmButton = {
            TextButton(onClick = {
                onApplyAndRestart()
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                @Suppress("ExitOutsideMain")
                Runtime.getRuntime().exit(0)
            }) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    notificationSoundUri: String?,
    onTimerSoundClick: () -> Unit,
    isBackupInProgress: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    versionName: String,
    osVersion: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ThemeSection(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            dynamicColor = dynamicColor,
            onDynamicColorChange = onDynamicColorChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SectionHeader(text = "Notifications")

        TimerSoundItem(
            soundUri = notificationSoundUri,
            onClick = onTimerSoundClick
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SectionHeader(text = "Data")

        ListItem(
            headlineContent = { Text("Export Data") },
            supportingContent = {
                Text(
                    if (isBackupInProgress) "Exporting..." else "Save a copy of all your data"
                )
            },
            modifier = if (isBackupInProgress) Modifier else Modifier.clickable(onClick = onExportClick)
        )

        ListItem(
            headlineContent = { Text("Import Data") },
            supportingContent = {
                Text(
                    if (isBackupInProgress) "Importing..." else "Restore data from a backup file"
                )
            },
            modifier = if (isBackupInProgress) Modifier else Modifier.clickable(onClick = onImportClick)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SectionHeader(text = "About")

        ListItem(
            headlineContent = { Text("NoCapFit") },
            supportingContent = { Text("Version $versionName") }
        )

        ListItem(
            headlineContent = { Text("OS Version") },
            supportingContent = { Text(osVersion) }
        )
    }
}

@Composable
private fun BackupDialogs(
    importUri: Uri?,
    hasActiveWorkout: Boolean,
    backupEvent: BackupEvent,
    onImportConfirm: (Uri) -> Unit,
    onImportDismiss: () -> Unit,
    onApplyAndRestart: () -> Unit
) {
    if (importUri != null) {
        ImportConfirmDialog(
            hasActiveWorkout = hasActiveWorkout,
            onConfirm = { onImportConfirm(importUri) },
            onDismiss = onImportDismiss
        )
    }

    if (backupEvent is BackupEvent.RestartRequired) {
        RestartDialog(
            context = LocalContext.current,
            onApplyAndRestart = onApplyAndRestart
        )
    }
}

private fun generateBackupFileName(): String {
    val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "nocapfit_backup_%04d-%02d-%02d_%02d-%02d.db".format(
        now.year,
        now.month.ordinal + 1,
        now.day,
        now.hour,
        now.minute
    )
}

private fun createRingtonePickerIntent(currentUri: String?): Intent {
    return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        val existingUri = when (currentUri) {
            null -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            NOTIFICATION_SOUND_SILENT -> null
            else -> currentUri.toUri()
        }
        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSection(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit
) {
    SectionHeader(text = "Appearance")

    ListItem(
        headlineContent = { Text("Theme") },
        supportingContent = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeMode.entries.size
                        ),
                        onClick = { onThemeModeChange(mode) },
                        selected = themeMode == mode
                    ) {
                        Text(
                            when (mode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System"
                            }
                        )
                    }
                }
            }
        }
    )

    ListItem(
        headlineContent = { Text("Dynamic Color") },
        supportingContent = { Text("Apply system theme colors") },
        trailingContent = {
            Switch(
                checked = dynamicColor,
                onCheckedChange = onDynamicColorChange
            )
        },
        modifier = Modifier.clickable { onDynamicColorChange(!dynamicColor) }
    )
}

@Composable
private fun TimerSoundItem(soundUri: String?, onClick: () -> Unit) {
    val context = LocalContext.current
    val soundName = remember(soundUri) {
        when (soundUri) {
            null -> "Default"
            NOTIFICATION_SOUND_SILENT -> "Silent"
            else -> try {
                val uri = soundUri.toUri()
                RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "Unknown"
            } catch (_: Exception) {
                "Unknown"
            }
        }
    }

    ListItem(
        headlineContent = { Text("Timer Sound") },
        supportingContent = { Text(soundName) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
