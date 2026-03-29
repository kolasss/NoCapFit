package com.example.nocapfit.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.BuildConfig
import com.example.nocapfit.data.preferences.ThemeMode
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val backupEvent by viewModel.backupEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    HandleBackupSnackbar(backupEvent, snackbarHostState, viewModel::clearBackupEvent)

    if (importUri != null) {
        ImportConfirmDialog(
            hasActiveWorkout = hasActiveWorkout,
            onConfirm = {
                val uri = importUri!!
                importUri = null
                viewModel.importDatabase(uri)
            },
            onDismiss = { importUri = null }
        )
    }

    if (backupEvent is BackupEvent.RestartRequired) {
        RestartDialog(context = context)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
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
            isBackupInProgress = backupEvent is BackupEvent.Exporting ||
                backupEvent is BackupEvent.Importing,
            onExportClick = {
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                val fileName = "nocapfit_backup_%04d-%02d-%02d_%02d-%02d.db".format(
                    now.year,
                    now.month.ordinal + 1,
                    now.day,
                    now.hour,
                    now.minute
                )
                exportLauncher.launch(fileName)
            },
            onImportClick = {
                importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            },
            versionName = BuildConfig.VERSION_NAME,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Data") },
        text = { Text(warningText) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RestartDialog(context: android.content.Context) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Data Restored") },
        text = { Text("Data restored successfully. The app will restart.") },
        confirmButton = {
            TextButton(onClick = {
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
    isBackupInProgress: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    versionName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Data",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

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

        Text(
            text = "About",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        ListItem(
            headlineContent = { Text("NoCapFit") },
            supportingContent = { Text("Version $versionName") }
        )
    }
}
