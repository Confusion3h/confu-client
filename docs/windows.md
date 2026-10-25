# Windows x64 installer

Build on Linux (Debian/Ubuntu):

    sudo apt-get install nsis imagemagick ffmpeg unzip curl
    JAVA_HOME=/opt/jdk8 scripts/build-windows-installer.sh /workspace/release

Output: `Confu-Client-Setup-<version>-win-x64.exe` (native x64 NSIS installer, per-user, no admin).

It installs to `%LOCALAPPDATA%\Programs\Confu Client`:

- `CONFUSION3H-launcher.jar`, `client\CONFUSION3H-Release.jar` (file names inside the install folder)
- `jre\` — Temurin 8 JRE x64 (sha256-pinned in the script); shortcuts run `jre\bin\javaw.exe`
- Start menu folder **Confu Client** (launcher, Music folder, Uninstall) and a Desktop shortcut with the C icon
- Uninstaller + "Apps & features" entry (HKCU), display name **Confu Client**

and creates `%APPDATA%\.confu\minecraft\glide\music` with a README and three 5 s
sample tones (WAV/OGG/MP3); existing files there are never overwritten.
If `%APPDATA%\.CONFUSION3H` is present and `.confu` is not, the installer renames
the old folder first, so worlds and settings move with it. The launcher does the
same rename on startup (Windows, Linux and macOS).

On Windows the launcher keeps its settings and game dir in `%APPDATA%\.confu`
and finds the client jar in `client\` next to itself. Uninstalling removes the program
only; worlds, settings, music and capes in `%APPDATA%\.confu` stay.

Silent install / uninstall: `Setup.exe /S`, `Uninstall.exe /S`.
x64 only; there is no ARM build.
