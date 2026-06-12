package com.asitsounds.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AsItSoundsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.asitsounds.android.asr.WhisperJni.loadNative()
    }
}
