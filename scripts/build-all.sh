#!/usr/bin/env bash
# Builds every Confu Client archive into releases/.
# Needs: JDK 8, makensis, ImageMagick, ffmpeg, curl, unzip, tar, python3, zip.
# macOS bundles are ad-hoc signed with rcodesign, which this script downloads.
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT=$PWD
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/temurin-8-jdk-amd64}"
OUT=$ROOT/releases
VERSION=0.1.0
CACHE=$OUT/cache
mkdir -p "$CACHE" "$OUT"

# Windows x64 installer (also runs gradle + launch-compat).
scripts/build-windows-installer.sh "$OUT"

RT_SHA=26e39d266b76e579919f5c2a55b9652823109da5417bee184bac9dbc2b667f72
RT_SRC=/usr/lib/jvm/temurin-8-jdk-amd64/jre/lib/rt.jar
echo "$RT_SHA  $RT_SRC" | sha256sum -c
cp "$RT_SRC" "$CACHE/java8-rt.jar"

fetch() {
  local url=$1 dest=$2 sha=$3
  if ! echo "$sha  $dest" | sha256sum -c --status 2>/dev/null; then
    curl -fL --retry 3 -o "$dest" "$url"
    echo "$sha  $dest" | sha256sum -c
  fi
}

# aka.ms tracks the current Microsoft Build of OpenJDK 17 Windows aarch64.
# Resolved 2026-10-25 to microsoft-jdk-17.0.20.1-windows-aarch64.zip.
fetch "https://aka.ms/download-jdk/microsoft-jdk-17-windows-aarch64.zip" \
  "$CACHE/msft-jdk17-win-aarch64.zip" "6872e702181fe88773a80bc3684281c111091e29e7f190ac5af06b0315f8b849"
fetch "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u504-b01/OpenJDK8U-jre_x64_linux_hotspot_8u504b01.tar.gz" \
  "$CACHE/temurin8-linux-x64.tar.gz" "52dcd578baca1d3e449ea86768a9129c0ee04d7b22565695498353cc66940c61"
fetch "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u504-b01/OpenJDK8U-jre_aarch64_linux_hotspot_8u504b01.tar.gz" \
  "$CACHE/temurin8-linux-aarch64.tar.gz" "9ae9c4dd80fc8f3c4081b480c7d42346e9e4cbee5ae58198fca11e0fc1a19163"
fetch "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u504-b01/OpenJDK8U-jre_x64_mac_hotspot_8u504b01.tar.gz" \
  "$CACHE/temurin8-mac-x64.tar.gz" "f06abadad0fa97e04866d7229a2baad53261579ab5d44875ffa8846a28f28d86"
fetch "https://cdn.azul.com/zulu/bin/zulu8.98.0.107-ca-jre8.0.512-macosx_aarch64.tar.gz" \
  "$CACHE/zulu8-mac-aarch64.tar.gz" "e363187e4c49650906b17466d5f158a2a488a0ed8dde296a54399fa45d19e895"

fetch "https://repo1.maven.org/maven2/org/glavo/hmcl/lwjgl2-natives/2.9.3-rc1-windows-arm64/lwjgl2-natives-2.9.3-rc1-windows-arm64.jar" \
  "$CACHE/lwjgl2-natives-windows-arm64.jar" "5fb601d1a7acdec075775d03280163004c90fecf3dcef3d7c0515cd38a6efbae"
fetch "https://repo1.maven.org/maven2/org/glavo/hmcl/lwjgl2-natives/2.9.3-rc1-osx-arm64/lwjgl2-natives-2.9.3-rc1-osx-arm64.jar" \
  "$CACHE/lwjgl2-natives-osx-arm64.jar" "17c9e680c0f2bc403b32cea340b4c32ff08e4e2d192e58ba1be6ca8aff705ba2"
fetch "https://repo1.maven.org/maven2/org/glavo/hmcl/lwjgl2-natives/2.9.3-linux-arm64/lwjgl2-natives-2.9.3-linux-arm64.jar" \
  "$CACHE/lwjgl2-natives-linux-arm64.jar" "c9fc25a6c5e40cc01d169dc8cea46d5f199a32c1d8e563661fb6c7355238fb78"

# apple-codesign 0.29.0. Ad-hoc signatures only; no hardened runtime.
ensure_rcodesign() {
  local dest=$CACHE/rcodesign
  if [[ ! -x "$dest" ]]; then
    local tar=$CACHE/apple-codesign-0.29.0-x86_64-unknown-linux-musl.tar.gz
    fetch "https://github.com/indygreg/apple-platform-rs/releases/download/apple-codesign/0.29.0/apple-codesign-0.29.0-x86_64-unknown-linux-musl.tar.gz" \
      "$tar" "dbe85cedd8ee4217b64e9a0e4c2aef92ab8bcaaa41f20bde99781ff02e600002"
    rm -rf "$CACHE/apple-codesign-0.29.0-x86_64-unknown-linux-musl"
    tar -xzf "$tar" -C "$CACHE"
    mv "$CACHE/apple-codesign-0.29.0-x86_64-unknown-linux-musl/rcodesign" "$dest"
    chmod 755 "$dest"
    rm -rf "$CACHE/apple-codesign-0.29.0-x86_64-unknown-linux-musl"
  fi
  export RCODESIGN=$dest
}

# --- unpack a JRE/JDK archive into dest/ with bin/java* at the top ---
unpack_jre() {
  local archive=$1 dest=$2
  rm -rf "$dest" "$dest.tmp"
  mkdir -p "$dest.tmp"
  case "$archive" in
    *.zip) unzip -q "$archive" -d "$dest.tmp" ;;
    *) tar -xzf "$archive" -C "$dest.tmp" ;;
  esac
  local top
  top=$(find "$dest.tmp" -type d -name bin -printf '%h\n' | head -1)
  if [[ -z "$top" ]]; then
    echo "No bin/ in $archive" >&2
    exit 1
  fi
  mv "$top" "$dest"
  # jmods duplicate lib/modules and are only needed to link a custom runtime.
  # ct.sym is a compiler symbol file. Neither is used to run the game.
  rm -rf "$dest.tmp" "$dest/src.zip" "$dest/lib/src.zip" "$dest/lib/ct.sym" \
    "$dest/sample" "$dest/demo" "$dest/man" "$dest/include" "$dest/jmods"
}

overlay_natives() {
  local jar=$1 dest=$2 os=$3
  mkdir -p "$dest"
  local tmp
  tmp=$(mktemp -d)
  unzip -q "$jar" -d "$tmp"
  case "$os" in
    windows)
      cp "$tmp/lwjgl64.dll" "$tmp/OpenAL64.dll" "$dest/"
      ;;
    osx)
      cp "$tmp/liblwjgl.dylib" "$tmp/openal.dylib" "$dest/"
      ;;
    linux)
      cp "$tmp/liblwjgl.so" "$dest/liblwjgl.so"
      cp "$tmp/liblwjgl.so" "$dest/liblwjgl64.so"
      cp "$tmp/libopenal.so" "$dest/libopenal.so"
      cp "$tmp/libopenal.so" "$dest/libopenal64.so"
      cp "$tmp/libjinput.so" "$dest/libjinput-linux.so"
      cp "$tmp/libjinput.so" "$dest/libjinput-linux64.so"
      ;;
  esac
  rm -rf "$tmp"
}

common_stage() {
  local dest=$1
  rm -rf "$dest"
  mkdir -p "$dest/client" "$dest/music"
  cp launcher/build/libs/CONFUSION3H-launcher-*.jar "$dest/CONFUSION3H-launcher.jar"
  cp launcher/build/libs/launch-compat.jar "$dest/launch-compat.jar"
  cp build/libs/CONFUSION3H-Release.jar "$dest/client/"
  cp LICENSE "$dest/LICENSE.txt"
  cp "$CACHE/java8-rt.jar" "$dest/java8-rt.jar"
  local M=$dest/music
  ffmpeg -loglevel error -y -f lavfi -i "sine=frequency=440:duration=5" -ac 2 "$M/Confu Client Sample - A440 (WAV).wav"
  ffmpeg -loglevel error -y -f lavfi -i "sine=frequency=660:duration=5" -ac 2 -b:a 128k "$M/Confu Client Sample - E660 (MP3).mp3"
  ffmpeg -loglevel error -y -f lavfi -i "sine=frequency=880:duration=5" -ac 2 -c:a libvorbis -q:a 3 "$M/Confu Client Sample - A880 (OGG).ogg"
  sed 's/$/\r/' scripts/windows/music-README.txt > "$M/README.txt"
}

# --- Windows ARM64 (x64 NSIS stub, native aarch64 JRE + natives) ---
ARMSTAGE=$OUT/stage-arm
common_stage "$ARMSTAGE"
unpack_jre "$CACHE/msft-jdk17-win-aarch64.zip" "$ARMSTAGE/jre"
overlay_natives "$CACHE/lwjgl2-natives-windows-arm64.jar" "$ARMSTAGE/natives/windows-arm64" windows
I=launcher/src/main/resources/confusione/icons
convert $I/icon_16x16.png $I/icon_32x32.png $I/icon_64x64.png $I/icon_128x128.png $I/icon_256x256.png \
  \( $I/icon_64x64.png -resize 48x48 \) -define png:exclude-chunks=all "$ARMSTAGE/confusion3h.ico"
# java8-rt.jar is only required on the Java 17 ARM package; common_stage copied it.
ARMEXE=$OUT/Confu-Client-Setup-$VERSION-win-arm64.exe
makensis -V2 -DARM64=1 -DSTAGE="$ARMSTAGE" -DOUTFILE="$ARMEXE" -DVERSION="$VERSION" \
  -DORIGINAL="Confu-Client-Setup-$VERSION-win-arm64.exe" \
  scripts/windows/CONFUSION3H.nsi
scripts/sign-windows.sh "$ARMEXE"

# Drop java8-rt from packages that run on Java 8 (saves ~63 MB and it is unused).
pack_linux() {
  local name=$1 archive=$2 osname=$3
  local stage=$OUT/stage-$name
  common_stage "$stage"
  rm -f "$stage/java8-rt.jar"
  unpack_jre "$archive" "$stage/jre"
  if [[ -n "$osname" ]]; then
    overlay_natives "$CACHE/lwjgl2-natives-${osname}-arm64.jar" "$stage/natives/${osname}-arm64" "$osname"
  fi
  cat > "$stage/confu" << 'EOF'
#!/bin/sh
cd "$(dirname "$0")"
exec ./jre/bin/java -Dsun.java2d.dpiaware=true -jar ./CONFUSION3H-launcher.jar "$@"
EOF
  chmod +x "$stage/confu"
  cat > "$stage/README.txt" << EOF
Confu Client $VERSION ($name)
Run ./confu
The bundled JRE is Temurin 8. Game files download on first launch into ~/.confu
EOF
  local rootname=Confu-Client-$VERSION-$name
  rm -rf "$OUT/$rootname"
  mv "$stage" "$OUT/$rootname"
  tar -C "$OUT" -czf "$OUT/$rootname.tar.gz" "$rootname"
  rm -rf "$OUT/$rootname"
}

pack_mac() {
  local name=$1 archive=$2 arm=$3
  local stage=$OUT/stage-$name
  common_stage "$stage"
  rm -f "$stage/java8-rt.jar"
  local app="$stage/Confu Client.app"
  mkdir -p "$app/Contents/MacOS" "$app/Contents/Resources"
  unpack_jre "$archive" "$app/Contents/Resources/jre"
  mv "$stage/CONFUSION3H-launcher.jar" "$stage/launch-compat.jar" "$stage/LICENSE.txt" "$app/Contents/Resources/"
  mv "$stage/client" "$app/Contents/Resources/client"
  if [[ "$arm" == arm ]]; then
    overlay_natives "$CACHE/lwjgl2-natives-osx-arm64.jar" "$app/Contents/Resources/natives/osx-arm64" osx
  fi
  # Some JRE files ship read-only (0444). Owner write is needed so that
  # xattr -dr com.apple.quarantine can clear them on the Mac.
  chmod -R u+w "$app"
  # Vendor seals (Zulu, the bundled framework) do not match the re-signed Mach-O files.
  find "$app/Contents" -name _CodeSignature -type d -prune -exec rm -rf {} +
  # Temurin's jre/bundle holds a second copy of libjvm for Apple's app stub. The
  # launcher loads lib/server/libjvm.dylib, so the copy is dropped.
  rm -rf "$app/Contents/Resources/jre/bundle"
  # Paths come from the launcher's own location, so the bundle works in
  # ~/Downloads or any other folder. Nothing points at /Applications.
  # No -XstartOnFirstThread here or in the game JVM: the Swing launcher deadlocks
  # with it, and vanilla 1.8.9 on macOS does not pass it.
  cp scripts/macos/confu-launcher.sh "$app/Contents/MacOS/confu"
  chmod 755 "$app/Contents/MacOS/confu"
  # The JRE's java runs from inside Contents/MacOS, so macOS treats the bundle as
  # the process's app: Info.plist (NSHighResolutionCapable, CFBundleIconFile) and
  # the Dock icon come from it. Contents/lib lets the java launcher find libjli
  # next to that path, as it does from Resources/jre/bin.
  ln -s ../Resources/jre/bin/java "$app/Contents/MacOS/confu-java"
  ln -s Resources/jre/lib "$app/Contents/lib"
  python3 scripts/macos/make-icns.py branding/png/mac "$app/Contents/Resources/confusion3h.icns"
  local machoarch=x86_64
  [[ "$arm" == arm ]] && machoarch=arm64
  # Lowest macOS the bundled Mach-O files load on: 11.0 for the Temurin 8 x64 JRE,
  # 12.0 for the arm64 LWJGL openal.dylib native.
  local min=11.0
  [[ "$arm" == arm ]] && min=12.0
  printf 'APPL????' > "$app/Contents/PkgInfo"
  cat > "$app/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
<key>CFBundleName</key><string>Confu Client</string>
<key>CFBundleDisplayName</key><string>Confu Client</string>
<key>CFBundleIdentifier</key><string>com.confu.client</string>
<key>CFBundleVersion</key><string>$VERSION</string>
<key>CFBundleShortVersionString</key><string>$VERSION</string>
<key>CFBundleInfoDictionaryVersion</key><string>6.0</string>
<key>CFBundleExecutable</key><string>confu</string>
<key>CFBundlePackageType</key><string>APPL</string>
<key>CFBundleSignature</key><string>????</string>
<key>CFBundleIconFile</key><string>confusion3h</string>
<key>LSMinimumSystemVersion</key><string>$min</string>
<key>NSHighResolutionCapable</key><true/>
<key>NSSupportsAutomaticGraphicsSwitching</key><true/>
<key>NSAppleEventsUsageDescription</key><string>Confu Client uses System Events to bring the Minecraft window to the front when the game starts.</string>
</dict></plist>
EOF
  # Sign only after Info.plist, PkgInfo, the JRE, natives and jars are in place.
  # Nothing in the bundle is written again after this returns.
  python3 scripts/macos/sign-app.py sign "$app" "$machoarch"
  local zip=$OUT/Confu-Client-$VERSION-$name.zip
  rm -f "$zip"
  # -y stores symlinks as links. Info-ZIP also records the Unix mode, so the
  # executable bit survives unzip. Do not pass -X (that drops the mode).
  (cd "$stage" && zip -r -y -q "$zip" "Confu Client.app")
  local check
  check=$(mktemp -d)
  unzip -q "$zip" -d "$check"
  python3 scripts/macos/sign-app.py compare "$app" "$check/Confu Client.app"
  python3 scripts/macos/sign-app.py verify "$check/Confu Client.app" "$machoarch"
  rm -rf "$check" "$stage"
}

ensure_rcodesign

pack_linux "linux-x64" "$CACHE/temurin8-linux-x64.tar.gz" ""
pack_linux "linux-aarch64" "$CACHE/temurin8-linux-aarch64.tar.gz" linux
pack_mac "mac-x64" "$CACHE/temurin8-mac-x64.tar.gz" ""
pack_mac "mac-aarch64" "$CACHE/zulu8-mac-aarch64.tar.gz" arm

rm -rf "$OUT/stage" "$OUT/stage-arm"

echo "---- artifacts ----"
ls -lh \
  "$OUT/Confu-Client-Setup-$VERSION-win-x64.exe" \
  "$OUT/Confu-Client-Setup-$VERSION-win-arm64.exe" \
  "$OUT/Confu-Client-$VERSION-linux-x64.tar.gz" \
  "$OUT/Confu-Client-$VERSION-linux-aarch64.tar.gz" \
  "$OUT/Confu-Client-$VERSION-mac-x64.zip" \
  "$OUT/Confu-Client-$VERSION-mac-aarch64.zip"
sha256sum \
  "$OUT/Confu-Client-Setup-$VERSION-win-x64.exe" \
  "$OUT/Confu-Client-Setup-$VERSION-win-arm64.exe" \
  "$OUT/Confu-Client-$VERSION-linux-x64.tar.gz" \
  "$OUT/Confu-Client-$VERSION-linux-aarch64.tar.gz" \
  "$OUT/Confu-Client-$VERSION-mac-x64.zip" \
  "$OUT/Confu-Client-$VERSION-mac-aarch64.zip"
