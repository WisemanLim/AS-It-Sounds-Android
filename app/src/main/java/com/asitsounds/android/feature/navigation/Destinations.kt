package com.asitsounds.android.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Filled.Home),
    LIBRARY("library", "라이브러리", Icons.Filled.Inventory2),
    PROJECTS("projects", "프로젝트", Icons.Filled.GraphicEq),
    SETTINGS("settings", "설정", Icons.Filled.Settings)
}

object Routes {
    const val ONBOARDING = "onboarding"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val MODEL_MANAGER = "settings/models"
    const val TRANSLATION_ENGINE = "settings/translation"
    const val ACCESSIBILITY = "settings/accessibility"
    const val ABOUT = "settings/about"
    const val LICENSES = "settings/licenses"
    const val REALTIME = "realtime"

    fun projectDetail(projectId: Long) = "project/$projectId"

    fun extractProjectId(route: String?): Long? =
        route?.substringAfterLast("project/")?.substringBefore("/")?.toLongOrNull()
}
