package com.asitsounds.android.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

// Dynamic "최근 프로젝트 열기" shortcuts (Android Shortcuts API; iOS App Intents 대응).
object AppShortcuts {

    const val MAX_SHORTCUTS = 4
    private const val ID_PREFIX = "project_"

    fun shortcutId(projectId: Long) = "$ID_PREFIX$projectId"

    fun projectIdFromShortcut(id: String?): Long? =
        id?.removePrefix(ID_PREFIX)?.toLongOrNull()

    fun pushRecentProjects(
        context: Context,
        projects: List<Pair<Long, String>>,   // (id, name)
        activityClass: Class<*>
    ) {
        val shortcuts = projects.take(MAX_SHORTCUTS).map { (id, name) ->
            val intent = Intent(context, activityClass).apply {
                action = Intent.ACTION_VIEW
                putExtra("projectId", id)
            }
            ShortcutInfoCompat.Builder(context, shortcutId(id))
                .setShortLabel(name.take(10))
                .setLongLabel(name)
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_recent_history))
                .setIntent(intent)
                .build()
        }
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
}
