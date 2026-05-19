package com.forgetrack.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.forgetrack.app.ui.navigation.ForgeTrackNavigation
import com.forgetrack.app.ui.navigation.Routes
import com.forgetrack.app.ui.screens.dashboard.DashboardViewModel
import com.forgetrack.app.ui.theme.ForgeTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                    val viewModel: DashboardViewModel by hiltViewModel()
                    val isOnboarded by viewModel.isOnboarded.collectAsState()
                    val navController = rememberNavController()

                    ForgeTrackNavigation(
                        navController = navController,
                        startDestination = if (isOnboarded) Routes.DASHBOARD else Routes.ONBOARDING
                    )
                }
            }
        }
    }
}
