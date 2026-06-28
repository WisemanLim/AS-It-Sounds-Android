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
    ),
    MEDIUM(
        id = "medium",
        filename = "ggml-medium.bin",
        sha256 = "fd9727b6d1c8e1bde76a132eb7e4b47838c8dfee1e4869bf5c9765e02c0cb87c",
        sizeBytes = 1_534_000_000L,
        minRamBytes = 8L * 1024 * 1024 * 1024
    ),
    // 추천: large-v3 대비 6× 빠름, 품질 동급 (ggerganov/whisper.cpp)
    LARGE_V3_TURBO(
        id = "large-v3-turbo",
        filename = "ggml-large-v3-turbo.bin",
        sha256 = "4af6b50bbf3f00f40f0a49cdffedc00e7bd66eda5bf27ade0b8bccc9f0e1c26c",
        sizeBytes = 809_000_000L,
        minRamBytes = 6L * 1024 * 1024 * 1024
    );

    companion object {
        private const val DOWNLOAD_BASE = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main"
        fun downloadUrl(model: WhisperModel) = "$DOWNLOAD_BASE/${model.filename}"
    }
}
