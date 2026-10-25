#!/usr/bin/env bash
# Builds launch-compat.jar (Java 9+ LaunchWrapper + Mixin 0.7 class-byte fallback).
# Usage: scripts/build-launch-compat.sh [out.jar]
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT=$PWD
OUT=${1:-$ROOT/launcher/build/libs/launch-compat.jar}
J8=${JAVA_HOME:-/usr/lib/jvm/temurin-8-jdk-amd64}
LW=${LW_JAR:-$ROOT/releases/cache/launchwrapper-1.12.jar}
ASM=${ASM_JAR:-$ROOT/releases/cache/asm-all-5.0.3.jar}
CLIENT=${CLIENT_JAR:-$ROOT/build/libs/CONFUSION3H-Release.jar}
mkdir -p "$(dirname "$OUT")" "$ROOT/releases/cache" /tmp/launch-compat-classes

if [[ ! -f "$LW" ]]; then
  curl -fsSL -o "$LW" https://libraries.minecraft.net/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar
fi
if [[ ! -f "$ASM" ]]; then
  curl -fsSL -o "$ASM" https://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar
fi
if [[ ! -f "$CLIENT" ]]; then
  echo "Client jar missing: $CLIENT" >&2
  exit 1
fi

"$J8/bin/javac" -source 1.8 -target 1.8 -d /tmp/launch-compat-classes \
  "$ROOT/scripts/launchcompat/ClasspathUrls.java" \
  "$ROOT/scripts/launchcompat/JdkClassBytes.java"
"$J8/bin/javac" -source 1.8 -target 1.8 -cp "$ASM" -d /tmp/launch-compat-classes \
  "$ROOT/scripts/launchcompat/BuildCompat.java"
"$J8/bin/java" -cp "/tmp/launch-compat-classes:$ASM" BuildCompat "$LW" "$CLIENT" /tmp/launch-compat-classes "$OUT"
ls -l "$OUT"
