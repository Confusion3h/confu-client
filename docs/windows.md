# Windows x64 installer

Build on Linux (Debian/Ubuntu):

    sudo apt-get install nsis imagemagick ffmpeg unzip curl
    JAVA_HOME=/opt/jdk8 scripts/build-windows-installer.sh /workspace/release

Output: `CONFUSION3H-Setup-<version>-win-x64.exe` (native x64 NSIS installer, per-user, no admin).

It installs to `%LOCALAPPDATA%\Programs\CONFUSION3H`:

- `CONFUSION3H-launcher.jar`, `client\CONFUSION3H-Release.jar`
- `jre\` — Temurin 8 JRE x64 (sha256-pinned in the script); shortcuts run `jre\bin\javaw.exe`
- Start menu (launcher, Music folder, Uninstall) and Desktop shortcut with the C icon
- Uninstaller + "Apps & features" entry (HKCU)

and creates `%APPDATA%\.CONFUSION3H\minecraft\glide\music` with a README and three 5 s
sample tones (WAV/OGG/MP3); existing files there are never overwritten.

On Windows the launcher keeps its settings and game dir in `%APPDATA%\.CONFUSION3H`
and finds the client jar in `client\` next to itself. Uninstalling removes the program
only; worlds, settings, music and capes in `%APPDATA%\.CONFUSION3H` stay.

Silent install / uninstall: `Setup.exe /S`, `Uninstall.exe /S`.
x64 only; there is no ARM build.
