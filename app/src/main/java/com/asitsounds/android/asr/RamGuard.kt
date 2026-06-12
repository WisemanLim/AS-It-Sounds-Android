package com.asitsounds.android.asr

import android.app.ActivityManager
import android.content.Context

object RamGuard {

    fun recommendedModel(context: Context): WhisperModel {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return modelForRam(mi.totalMem)
    }

    // Internal so unit tests can exercise without Android context
    internal fun modelForRam(totalRamBytes: Long): WhisperModel = when {
        totalRamBytes >= 6L * 1024 * 1024 * 1024 -> WhisperModel.SMALL
        totalRamBytes >= 3L * 1024 * 1024 * 1024 -> WhisperModel.BASE
        else                                      -> WhisperModel.TINY
    }
}
