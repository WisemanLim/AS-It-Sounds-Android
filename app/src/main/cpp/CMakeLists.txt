cmake_minimum_required(VERSION 3.22.1)
project("whisper_jni")

set(WHISPER_DIR ${CMAKE_CURRENT_SOURCE_DIR}/whisper)

if(EXISTS ${WHISPER_DIR}/CMakeLists.txt)
    # --- Real build: whisper.cpp repo present (run scripts/setup_whisper.sh) ---
    message(STATUS "whisper.cpp found — building real ASR backend")

    set(WHISPER_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
    set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(WHISPER_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
    set(GGML_OPENMP            OFF CACHE BOOL "" FORCE)
    add_subdirectory(${WHISPER_DIR} ${CMAKE_BINARY_DIR}/whisper_build)

    add_library(whisper_jni SHARED whisper_jni.cpp)
    target_link_libraries(whisper_jni whisper)          # propagates whisper.h include dir
    target_compile_options(whisper_jni PRIVATE -O3 -ffast-math -DNDEBUG)
else()
    # --- Stub build: lets the APK assemble + load before sources are fetched ---
    message(WARNING "whisper.cpp NOT found at ${WHISPER_DIR} — building STUB. "
                    "Run scripts/setup_whisper.sh and rebuild for real ASR.")
    add_library(whisper_jni SHARED whisper_jni_stub.cpp)
endif()

find_library(log-lib log)
find_library(android-lib android)
target_link_libraries(whisper_jni ${log-lib} ${android-lib})
