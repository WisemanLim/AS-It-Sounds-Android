package com.asitsounds.android.feature.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.asitsounds.android.feature.home.HomeScreen
import com.asitsounds.android.feature.library.LibraryScreen
import com.asitsounds.android.feature.project.ProjectDetailScreen
import com.asitsounds.android.feature.project.ProjectsScreen
import com.asitsounds.android.feature.realtime.RealtimeScreen
import com.asitsounds.android.feature.settings.AboutScreen
import com.asitsounds.android.feature.settings.AccessibilityScreen
import com.asitsounds.android.feature.settings.LicensesScreen
import com.asitsounds.android.feature.settings.ModelManagerScreen
import com.asitsounds.android.feature.settings.SettingsScreen
import com.asitsounds.android.feature.settings.TranslationEngineScreen

// Tablet breakpoint per Material guidance.
private const val TABLET_MIN_WIDTH_DP = 600

@Composable
fun AppShell(
    navController: NavHostController = rememberNavController(),
    onNewTranscription: () -> Unit = {}
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= TABLET_MIN_WIDTH_DP
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val onSelect: (TopDestination) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun selected(dest: TopDestination): Boolean =
        currentRoute?.hierarchy?.any { it.route == dest.route } == true

    if (isTablet) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail {
                TopDestination.entries.forEach { dest ->
                    NavigationRailItem(
                        selected = selected(dest),
                        onClick = { onSelect(dest) },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
            AppNavHost(navController, onNewTranscription, onSelect, Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TopDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = selected(dest),
                            onClick = { onSelect(dest) },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        ) { padding ->
            AppNavHost(navController, onNewTranscription, onSelect, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    onNewTranscription: () -> Unit,
    onSelectTab: (TopDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = TopDestination.HOME.route, modifier = modifier) {
        composable(TopDestination.HOME.route) {
            HomeScreen(
                onNewTranscription = onNewTranscription,
                onOpenProject = { navController.navigate(Routes.projectDetail(it)) },
                // Model manager is a detail screen (back arrow → home), not a tab —
                // so 홈 stays consistent. Gear switches to the Settings tab.
                onOpenModelManager = { navController.navigate(Routes.MODEL_MANAGER) },
                onOpenSettings = { onSelectTab(TopDestination.SETTINGS) },
                onOpenRealtime = { navController.navigate(Routes.REALTIME) }
            )
        }
        composable(TopDestination.LIBRARY.route) {
            LibraryScreen(onOpenProject = { navController.navigate(Routes.projectDetail(it)) })
        }
        composable(TopDestination.PROJECTS.route) {
            ProjectsScreen(onOpenProject = { navController.navigate(Routes.projectDetail(it)) })
        }
        composable(TopDestination.SETTINGS.route) {
            SettingsScreen(
                onOpenModelManager = { navController.navigate(Routes.MODEL_MANAGER) },
                onOpenTranslationEngine = { navController.navigate(Routes.TRANSLATION_ENGINE) },
                onOpenAccessibility = { navController.navigate(Routes.ACCESSIBILITY) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onOpenLicenses = { navController.navigate(Routes.LICENSES) }
            )
        }
        composable(Routes.MODEL_MANAGER) {
            ModelManagerScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRANSLATION_ENGINE) {
            TranslationEngineScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ACCESSIBILITY) { AccessibilityScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.LICENSES) { LicensesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.REALTIME) {
            RealtimeScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("projectId") ?: -1L
            ProjectDetailScreen(projectId = id, onBack = { navController.popBackStack() })
        }
    }
}
