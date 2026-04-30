#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

if ! command -v javac >/dev/null 2>&1; then
  fail "javac was not found. Install a JDK first, for example: sudo apt-get install openjdk-21-jdk"
fi

ADB_BIN="${ADB:-}"
if [[ -z "$ADB_BIN" && -f local.properties ]]; then
  SDK_DIR="$(sed -n 's/^sdk\.dir=//p' local.properties | tail -n 1)"
  if [[ -n "$SDK_DIR" ]]; then
    ADB_BIN="$SDK_DIR/platform-tools/adb"
  fi
fi

if [[ -z "$ADB_BIN" && -n "${ANDROID_HOME:-}" ]]; then
  ADB_BIN="$ANDROID_HOME/platform-tools/adb"
fi

if [[ -z "$ADB_BIN" && -n "${ANDROID_SDK_ROOT:-}" ]]; then
  ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"
fi

if [[ -z "$ADB_BIN" ]]; then
  ADB_BIN="adb"
fi

if ! command -v "$ADB_BIN" >/dev/null 2>&1; then
  fail "adb was not found. Set ANDROID_HOME/ANDROID_SDK_ROOT, or pass ADB=/path/to/adb."
fi

if ! "$ADB_BIN" get-state >/dev/null 2>&1; then
  "$ADB_BIN" devices -l
  fail "No connected Android device or emulator is ready."
fi

./gradlew clean installDebug

PACKAGE_NAME="com.example.dadn_app"
"$ADB_BIN" shell am force-stop "$PACKAGE_NAME" >/dev/null 2>&1 || true
"$ADB_BIN" shell monkey -p "$PACKAGE_NAME" 1 >/dev/null

printf '\nInstalled and launched %s from the current source tree.\n' "$PACKAGE_NAME"
