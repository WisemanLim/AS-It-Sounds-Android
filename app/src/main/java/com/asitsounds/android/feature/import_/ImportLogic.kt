package com.asitsounds.android.feature.import_

// Pure import helpers — file naming, mime support, extension mapping.
object ImportLogic {

    val SUPPORTED_PREFIXES = listOf("audio/", "video/")

    fun isSupportedMime(mime: String?): Boolean {
        if (mime == null) return false
        return SUPPORTED_PREFIXES.any { mime.startsWith(it) }
    }

    // Strip path separators and characters illegal on Android filesystems.
    fun sanitizeFileName(name: String): String {
        val cleaned = name
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .replace(Regex("[^a-zA-Z0-9가-힣._ -]"), "_")
            .trim()
        return cleaned.ifEmpty { "import" }
    }

    // Ensure uniqueness against existing names by appending _1, _2, ...
    fun uniqueName(desired: String, existing: Set<String>): String {
        if (desired !in existing) return desired
        val dot = desired.lastIndexOf('.')
        val base = if (dot > 0) desired.substring(0, dot) else desired
        val ext = if (dot > 0) desired.substring(dot) else ""
        var i = 1
        while ("${base}_$i$ext" in existing) i++
        return "${base}_$i$ext"
    }

    fun extensionForMime(mime: String?): String = when (mime) {
        "audio/mpeg", "audio/mp3" -> "mp3"
        "audio/mp4", "audio/m4a", "audio/x-m4a" -> "m4a"
        "audio/wav", "audio/x-wav" -> "wav"
        "audio/ogg" -> "ogg"
        "audio/flac" -> "flac"
        "video/mp4" -> "mp4"
        "video/quicktime" -> "mov"
        "video/x-matroska" -> "mkv"
        "video/webm" -> "webm"
        else -> "bin"
    }

    // Derive a display name when the picker provides none (use mime + fallback).
    fun displayNameOrDefault(name: String?, mime: String?): String =
        name?.takeIf { it.isNotBlank() }?.let { sanitizeFileName(it) }
            ?: "import.${extensionForMime(mime)}"
}

enum class ImportSource { DOCUMENT, MEDIA, SHARED }
