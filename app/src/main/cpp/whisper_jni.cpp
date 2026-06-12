#include <jni.h>
#include <android/log.h>
#include <string>
#include <sstream>
#include "whisper.h"

#define TAG "WhisperJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_asitsounds_android_asr_WhisperJni_initContext(
        JNIEnv *env, jobject /*obj*/, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false;
    whisper_context *ctx = whisper_init_from_file_with_params(path, cparams);
    env->ReleaseStringUTFChars(modelPath, path);
    if (!ctx) {
        LOGE("Failed to init whisper context");
        return 0L;
    }
    LOGI("whisper context initialized: %p", (void*)ctx);
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL
Java_com_asitsounds_android_asr_WhisperJni_freeContext(
        JNIEnv * /*env*/, jobject /*obj*/, jlong ctxPtr) {
    if (ctxPtr == 0L) return;
    whisper_free(reinterpret_cast<whisper_context *>(ctxPtr));
    LOGI("whisper context freed");
}

// Returns newline-delimited "startMs|endMs|text|lang" records
JNIEXPORT jstring JNICALL
Java_com_asitsounds_android_asr_WhisperJni_transcribeNative(
        JNIEnv *env, jobject /*obj*/, jlong ctxPtr,
        jfloatArray audioData, jstring language) {
    if (ctxPtr == 0L) return env->NewStringUTF("");

    auto *ctx = reinterpret_cast<whisper_context *>(ctxPtr);
    jsize nSamples = env->GetArrayLength(audioData);
    jfloat *samples = env->GetFloatArrayElements(audioData, nullptr);
    const char *lang = env->GetStringUTFChars(language, nullptr);

    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_progress   = false;
    params.print_special    = false;
    params.print_realtime   = false;
    params.print_timestamps = true;
    params.translate        = false;
    params.language         = (lang && strlen(lang) > 0) ? lang : "auto";
    params.n_threads        = 4;
    params.no_context       = true;
    params.single_segment   = false;

    int rc = whisper_full(ctx, params, samples, static_cast<int>(nSamples));

    env->ReleaseFloatArrayElements(audioData, samples, JNI_ABORT);
    env->ReleaseStringUTFChars(language, lang);

    if (rc != 0) {
        LOGE("whisper_full failed: %d", rc);
        return env->NewStringUTF("");
    }

    int n = whisper_full_n_segments(ctx);
    const int langId = whisper_full_lang_id(ctx);
    const char *det = (langId >= 0) ? whisper_lang_str(langId) : "en";
    std::ostringstream sb;
    for (int i = 0; i < n; i++) {
        // whisper timestamps are in centiseconds (1/100 s), convert to ms
        int64_t t0 = whisper_full_get_segment_t0(ctx, i) * 10;
        int64_t t1 = whisper_full_get_segment_t1(ctx, i) * 10;
        const char *text = whisper_full_get_segment_text(ctx, i);
        sb << t0 << "|" << t1 << "|"
           << (text ? text : "") << "|"
           << (det  ? det  : "en") << "\n";
    }
    return env->NewStringUTF(sb.str().c_str());
}

} // extern "C"
