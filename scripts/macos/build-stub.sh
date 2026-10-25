#!/bin/sh
# Cross-link the macOS app executable (Contents/MacOS/confu).
# Usage: build-stub.sh arm64|x64 /path/to/confu
set -eu
ARCH=${1:?arch}
OUT=${2:?out}
ROOT=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
SRC="$ROOT/confu-stub.c"
case "$ARCH" in
  arm64)
    TARGET=arm64-apple-macos11
    LDARCH=arm64
    MIN=11.0.0
    ;;
  x64)
    TARGET=x86_64-apple-macos10.13
    LDARCH=x86_64
    MIN=10.13.0
    ;;
  *)
    echo "arch must be arm64 or x64" >&2
    exit 2
    ;;
esac
LD64=$(command -v ld64.lld || true)
if [ -z "$LD64" ]; then
  LD64=$(ls /usr/lib/llvm-*/bin/ld64.lld 2>/dev/null | sort | tail -1)
fi
if [ -z "$LD64" ]; then
  echo "ld64.lld not found" >&2
  exit 1
fi
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
cat > "$TMP/libsys.s" << 'EOF'
.globl __exit
.globl _chdir
.globl _execve
.globl _NSGetExecutablePath
.globl dyld_stub_binder
__exit:
  ret
_chdir:
  ret
_execve:
  ret
_NSGetExecutablePath:
  ret
dyld_stub_binder:
  ret
EOF
clang -target "$TARGET" -c "$TMP/libsys.s" -o "$TMP/libsys.o"
"$LD64" -dylib -arch "$LDARCH" -platform_version macos "$MIN" "$MIN" \
  -install_name /usr/lib/libSystem.B.dylib -o "$TMP/libSystem.dylib" "$TMP/libsys.o"
clang -target "$TARGET" -fno-stack-protector -fno-builtin -Os -c "$SRC" -o "$TMP/stub.o"
"$LD64" -arch "$LDARCH" -platform_version macos "$MIN" "$MIN" \
  -o "$OUT" "$TMP/stub.o" "$TMP/libSystem.dylib"
chmod 755 "$OUT"
