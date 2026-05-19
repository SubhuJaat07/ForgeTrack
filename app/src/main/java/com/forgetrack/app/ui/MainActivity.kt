package com.forgetrack.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.forgetrack.app.data.local.UserPreferences
import com.forgetrack.app.ui.navigation.ForgeTrackNavigation
import com.forgetrack.app.ui.navigation.Routes
import com.forgetrack.app.ui.screens.update.UpdateAvailableDialog
import com.forgetrack.app.ui.screens.update.UpdateViewModel
import com.forgetrack.app.ui.theme.ForgeTrackTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgeTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isOnboarded by userPreferences.isOnboarded
                        .collectAsStateWithLifecycle(initialValue = false)

                    val updateViewModel: UpdateViewModel = hiltViewModel()
                    val navController = rememberNavController()

                    // Check for updates on first composition
                    LaunchedEffect(Unit) {
                        updateViewModel.checkForUpdateSilent()
                    }

                    // If notification tapped to show update, trigger manual check
                    if (intent?.getBooleanExtra("show_update", false) == true) {
                        LaunchedEffect(Unit) {
                            updateViewModel.checkForUpdateManual()
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        ForgeTrackNavigation(
                            navController = navController,
                            startDestination = if (isOnboarded) Routes.DASHBOARD else Routes.ONBOARDING
                        )

                        // Global update dialog - shown on top of everything
                        UpdateAvailableDialog(viewModel = updateViewModel)
                    }
                }
            }
        }
    }
}
