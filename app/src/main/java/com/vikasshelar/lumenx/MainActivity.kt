package com.vikasshelar.lumenx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vikasshelar.lumenx.core.designsystem.LumenXTheme
import com.vikasshelar.lumenx.feature.flashlight.FlashlightRoute
import com.vikasshelar.lumenx.feature.settings.SettingsRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumenXTheme {
                LumenXNavHost()
            }
        }
    }
}

@Composable
private fun LumenXNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LumenRoute.Flashlight.route,
    ) {
        composable(LumenRoute.Flashlight.route) {
            FlashlightRoute(
                onSettingsClick = { navController.navigate(LumenRoute.Settings.route) },
            )
        }
        composable(LumenRoute.Settings.route) {
            SettingsRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private enum class LumenRoute(val route: String) {
    Flashlight("flashlight"),
    Settings("settings"),
}
