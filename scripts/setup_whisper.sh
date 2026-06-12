#!/usr/bin/env bash
# Fetch whisper.cpp sources for the Android JNI build (official add_subdirectory pattern).
# Run once after cloning this repo, before building with NDK.
set -euo pipefail

WHISPER_TAG="${WHISPER_TAG:-v1.7.4}"
DEST="app/src/main/cpp/whisper"
REPO="https://github.com/ggerganov/whisper.cpp.git"

if [[ -f "${DEST}/CMakeLists.txt" ]]; then
  echo "==> whisper.cpp already present at ${DEST} (skip clone)."
else
  echo "==> Cloning whisper.cpp ${WHISPER_TAG} (shallow) → ${DEST}"
  git clone --depth 1 --branch "${WHISPER_TAG}" "${REPO}" "${DEST}"
fi

echo ""
echo "Done. Build real ASR with:"
echo "  ./gradlew assembleDevDebug"
echo ""
echo "(whisper.cpp is MIT-licensed, ggerganov. Sources are git-ignored — not committed.)"
