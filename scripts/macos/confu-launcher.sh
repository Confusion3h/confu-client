#!/bin/sh
# Contents/MacOS/confu. Launch Services runs this as the bundle's executable.
# It moves into Contents/Resources and replaces itself with the bundled Java 8
# launcher, so paths come from this file's own location.
# confu-java links to the JRE's java binary from inside Contents/MacOS. That keeps
# the process inside this bundle, so Info.plist (NSHighResolutionCapable, icon)
# applies to it. Without it, AppKit scales the launcher up from 1x on Retina.
# No -XstartOnFirstThread here: that flag deadlocks the Swing launcher. GameLauncher
# adds it only to the game JVM on macOS.
set -eu
here=$(cd "$(dirname "$0")" && pwd -P)
res=$(cd "$here/../Resources" && pwd -P)
cd "$res"
java="$here/confu-java"
if [ ! -x "$java" ]; then
  java="$res/jre/bin/java"
fi
exec "$java" \
  "-Dapple.awt.application.name=Confu Client" \
  "-Xdock:name=Confu Client" \
  "-Xdock:icon=$res/confusion3h.icns" \
  -Dsun.java2d.dpiaware=true \
  -Dapple.awt.graphics.UseQuartz=true \
  -Dawt.useSystemAAFontSettings=on \
  -Dswing.aatext=true \
  -jar "$res/CONFUSION3H-launcher.jar" "$@"
