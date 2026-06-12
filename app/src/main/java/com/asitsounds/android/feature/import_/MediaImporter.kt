package com.asitsounds.android.feature.import_

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

data class ImportedFile(
    val path: String,
    val displayName: String,
    val mime: String
)

// Copies a SAF/shared Uri into app-private storage (PRD §7.4: user files in filesDir copy).
class MediaImporter(private val context: Context) {

    private fun importsDir(): File = File(context.filesDir, "imports").also { it.mkdirs() }

    @Throws(IOException::class)
    suspend fun import(uri: Uri): ImportedFile = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "application/octet-stream"

        if (!ImportLogic.isSupportedMime(mime)) {
            throw IOException("Unsupported mime type: $mime")
        }

        val queriedName = queryDisplayName(uri)
        val displayName = ImportLogic.displayNameOrDefault(queriedName, mime)
        val existing = importsDir().list()?.toSet() ?: emptySet()
        val finalName = ImportLogic.uniqueName(displayName, existing)
        val dest = File(importsDir(), finalName)

        resolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Cannot open input stream for: $uri")

        ImportedFile(path = dest.absolutePath, displayName = finalName, mime = mime)
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) return it.getString(idx)
        }
        return null
    }
}
