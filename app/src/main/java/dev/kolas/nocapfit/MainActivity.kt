package dev.kolas.nocapfit

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.kolas.nocapfit.data.preferences.ThemePreferences
import dev.kolas.nocapfit.ui.components.BottomNavBar
import dev.kolas.nocapfit.ui.components.MiniWorkoutPanel
import dev.kolas.nocapfit.ui.navigation.NavGraph
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.ui.screens.main.ActiveWorkoutViewModel
import dev.kolas.nocapfit.ui.theme.NoCapFitTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no action needed on result */ }

    // Workout to open, set when the activity is launched from a rest timer notification.
    private var pendingWorkoutId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // On recreation the launch intent is redelivered; the nav back stack is already restored.
        if (savedInstanceState == null) pendingWorkoutId = intent.workoutIdExtra()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        var themeLoaded = false
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = null)
            val dynamicColor by themePreferences.dynamicColor.collectAsState(initial = null)
            val mode = themeMode
            val dynamic = dynamicColor
            if (mode != null && dynamic != null) {
                SideEffect { themeLoaded = true }
                NoCapFitTheme(themeMode = mode, dynamicColor = dynamic) {
                    MainContent(
                        pendingWorkoutId = pendingWorkoutId,
                        onPendingWorkoutOpened = { pendingWorkoutId = null }
                    )
                }
            }
        }
        // Hold the first frame (keeping the system splash visible) until the theme preference
        // is loaded, so the app never flashes the wrong theme on cold start.
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean = if (themeLoaded) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    false
                }
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.workoutIdExtra()?.let { pendingWorkoutId = it }
    }

    companion object {
        private const val EXTRA_WORKOUT_ID = "workout_id"

        private fun Intent.workoutIdExtra(): Long? =
            getLongExtra(EXTRA_WORKOUT_ID, -1L).takeIf { it != -1L }

        /** Opens the app on the in-progress screen of [workoutId]; used as a notification tap action. */
        fun openWorkoutPendingIntent(context: Context, workoutId: Long): PendingIntent {
            // SINGLE_TOP + CLEAR_TOP reuse the running activity (delivered via onNewIntent).
            val intent = Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_WORKOUT_ID, workoutId)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

private val BOTTOM_NAV_ROUTES = listOf(
    Screen.WorkoutHistory.route,
    Screen.ExerciseList.route,
    Screen.ProgramList.route
)

@Composable
private fun MainContent(
    pendingWorkoutId: Long?,
    onPendingWorkoutOpened: () -> Unit,
    activeWorkoutViewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    LaunchedEffect(pendingWorkoutId) {
        val workoutId = pendingWorkoutId ?: return@LaunchedEffect
        navController.navigateBottomNav(Screen.WorkoutInProgress.createRoute(workoutId))
        onPendingWorkoutOpened()
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeWorkout by activeWorkoutViewModel.activeWorkout.collectAsState()

    val isOnWorkoutScreen = currentRoute == Screen.WorkoutInProgress.route
    val isMinimized = activeWorkout != null && !isOnWorkoutScreen
    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES || isMinimized

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    showMiniPanel = isMinimized,
                    minimizedWorkoutName = activeWorkout?.programName,
                    minimizedWorkoutStartTime = activeWorkout?.startTime ?: 0L,
                    currentRoute = currentRoute,
                    onResume = {
                        val id = activeWorkout?.id ?: return@MainBottomBar
                        navController.navigateBottomNav(Screen.WorkoutInProgress.createRoute(id))
                    },
                    onNavigate = { screen ->
                        navController.navigateBottomNav(screen.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onMinimizeWorkout = {
                navController.navigate(Screen.WorkoutHistory.route) {
                    popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                }
            }
        )
    }
}

private fun NavController.navigateBottomNav(route: String) {
    navigate(route) {
        popUpTo(Screen.WorkoutHistory.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun MainBottomBar(
    showMiniPanel: Boolean,
    minimizedWorkoutName: String?,
    minimizedWorkoutStartTime: Long,
    currentRoute: String?,
    onResume: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    Column {
        AnimatedVisibility(
            visible = showMiniPanel,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            MiniWorkoutPanel(
                workoutName = minimizedWorkoutName ?: "Workout",
                startTimeMs = minimizedWorkoutStartTime,
                onClick = onResume
            )
        }
        BottomNavBar(
            currentRoute = currentRoute,
            onNavigate = onNavigate
        )
    }
}
