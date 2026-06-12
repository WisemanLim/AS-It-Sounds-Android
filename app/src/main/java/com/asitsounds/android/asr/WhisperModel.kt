package com.asitsounds.android.asr

enum class WhisperModel(
    val id: String,
    val filename: String,
    val sha256: String,
    val sizeBytes: Long,
    val minRamBytes: Long
) {
    TINY(
        id = "tiny",
        filename = "ggml-tiny.bin",
        sha256 = "be07e048e1e599ad46341c8d2a135645097a538221678b7acdd1b1919c6e1b21",
        sizeBytes = 75_000_000L,
        minRamBytes = 2L * 1024 * 1024 * 1024
    ),
    BASE(
        id = "base",
        filename = "ggml-base.bin",
        sha256 = "60ed5bc3dd14eea856493d334349b405782ddcaf0028d4b5df4088345fba2efe",
        sizeBytes = 142_000_000L,
        minRamBytes = 3L * 1024 * 1024 * 1024
    ),
    SMALL(
        id = "small",
        filename = "ggml-small.bin",
        sha256 = "1be3a9b2063867b937e64e2ec7483364a79917e157fa98c5d94b5c1fffea987b",
        sizeBytes = 466_000_000L,
        minRamBytes = 6L * 1024 * 1024 * 1024
    );

    companion object {
        private const val DOWNLOAD_BASE = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main"
        fun downloadUrl(model: WhisperModel) = "$DOWNLOAD_BASE/${model.filename}"
    }
}
