package com.asitsounds.android.pipeline

import android.content.Context
import android.net.Uri
import com.asitsounds.android.asr.AudioDecoder

// A pre-bound audio source: it already knows what to decode, so the
// orchestrator never has to touch Context/Uri directly.
interface AudioSource {
    suspend fun decode(): FloatArray
}

class DefaultAudioSource(
    private val context: Context,
    private val uri: Uri,
    private val decoder: AudioDecoder = AudioDecoder()
) : AudioSource {
    override suspend fun decode(): FloatArray = decoder.decode(context, uri)
}
