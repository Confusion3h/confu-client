#!/usr/bin/env bash
# Builds release/Confu-Client-Setup-<ver>-win-x64.exe on Linux.
# Needs: JDK 8 (JAVA_HOME), makensis (apt: nsis), ImageMagick (convert), curl, unzip.
# Usage: scripts/build-windows-installer.sh [outdir]   (default /workspace/release)
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT=$PWD
OUT=${1:-/workspace/release}
VERSION=$(cat "$ROOT/VERSION")
JRE_URL=https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u504-b01/OpenJDK8U-jre_x64_windows_hotspot_8u504b01.zip
JRE_SHA256=82e2cdc6693737c5998445b31f69668fa0da77c7705121053f6508ac84961123
CACHE=$OUT/cache
STAGE=$OUT/stage
mkdir -p "$CACHE" "$OUT"

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/temurin-8-jdk-amd64}"
./gradlew build -x getAssets --no-daemon -q
scripts/build-launch-compat.sh "$ROOT/launcher/build/libs/launch-compat.jar"

if ! echo "$JRE_SHA256  $CACHE/jre.zip" | sha256sum -c --status 2>/dev/null; then
  curl -fsSLo "$CACHE/jre.zip" "$JRE_URL"
  echo "$JRE_SHA256  $CACHE/jre.zip" | sha256sum -c
fi

rm -rf "$STAGE" && mkdir -p "$STAGE/client" "$STAGE/music"
cp launcher/build/libs/CONFUSION3H-launcher-*.jar "$STAGE/CONFUSION3H-launcher.jar"
cp launcher/build/libs/launch-compat.jar "$STAGE/launch-compat.jar"
cp build/libs/CONFUSION3H-Release.jar "$STAGE/client/"
cp LICENSE "$STAGE/LICENSE.txt"
unzip -q "$CACHE/jre.zip" -d "$STAGE/jre-tmp"
mv "$STAGE"/jre-tmp/*/ "$STAGE/jre" && rmdir "$STAGE/jre-tmp"
# demo/sample/src are not needed at runtime
rm -rf "$STAGE/jre/src.zip" "$STAGE/jre/sample" "$STAGE/jre/demo"

I=launcher/src/main/resources/confusione/icons
convert $I/icon_16x16.png $I/icon_32x32.png $I/icon_64x64.png $I/icon_128x128.png $I/icon_256x256.png \
  \( $I/icon_64x64.png -resize 48x48 \) -define png:exclude-chunks=all "$STAGE/confusion3h.ico"

M=$STAGE/music
sed 's/$/\r/' scripts/windows/music-README.txt > "$M/README.txt"

EXE="$OUT/Confu-Client-Setup-$VERSION-win-x64.exe"
makensis -V2 -DSTAGE="$STAGE" -DOUTFILE="$EXE" -DVERSION="$VERSION" \
  -DORIGINAL="Confu-Client-Setup-$VERSION-win-x64.exe" \
  scripts/windows/CONFUSION3H.nsi
# Signs only when CONFU_SIGN_PFX or CONFU_AZURE_DLIB is set. See scripts/sign-windows.sh.
scripts/sign-windows.sh "$EXE"
ls -l "$EXE"
