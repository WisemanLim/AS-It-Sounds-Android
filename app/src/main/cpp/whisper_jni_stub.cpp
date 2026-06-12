// Stub JNI backend — built when whisper.cpp sources are absent so the APK still
// assembles and the native library loads. Returns empty results until the real
// sources are fetched (scripts/setup_whisper.sh) and the project is rebuilt.
#include <jni.h>
#include <android/log.h>

#define TAG "WhisperJNI"
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_asitsounds_android_asr_WhisperJni_initContext(
        JNIEnv * /*env*/, jobject /*obj*/, jstring /*modelPath*/) {
    LOGW("STUB whisper_jni: initContext returns 0 (no native backend). "
         "Run scripts/setup_whisper.sh for real ASR.");
    return 0L;
}

JNIEXPORT void JNICALL
Java_com_asitsounds_android_asr_WhisperJni_freeContext(
        JNIEnv * /*env*/, jobject /*obj*/, jlong /*ctxPtr*/) {
    // no-op
}

JNIEXPORT jstring JNICALL
Java_com_asitsounds_android_asr_WhisperJni_transcribeNative(
        JNIEnv *env, jobject /*obj*/, jlong /*ctxPtr*/,
        jfloatArray /*audioData*/, jstring /*language*/) {
    LOGW("STUB whisper_jni: transcribeNative returns empty.");
    return env->NewStringUTF("");
}

} // extern "C"
