#!/bin/sh
# Contents/MacOS/confu. Launch Services runs this as the bundle's executable.
# It moves into Contents/Resources and replaces itself with the bundled Java 8
# launcher, so paths come from this file's own location.
# No -XstartOnFirstThread: that flag deadlocks the Swing launcher. GameLauncher
# adds it only to the game JVM on macOS.
set -eu
here=$(cd "$(dirname "$0")" && pwd -P)
res=$(cd "$here/../Resources" && pwd -P)
cd "$res"
exec "$res/jre/bin/java" \
  "-Dapple.awt.application.name=Confu Client" \
  "-Xdock:name=Confu Client" \
  -Dsun.java2d.dpiaware=true \
  -Dapple.awt.graphics.UseQuartz=true \
  -Dawt.useSystemAAFontSettings=on \
  -Dswing.aatext=true \
  -jar "$res/CONFUSION3H-launcher.jar" "$@"
