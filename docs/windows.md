# Windows x64 installer

Build on Linux (Debian/Ubuntu):

    sudo apt-get install nsis imagemagick ffmpeg unzip curl
    JAVA_HOME=/opt/jdk8 scripts/build-windows-installer.sh /workspace/release

Output: `Confu-Client-Setup-<version>-win-x64.exe` (native x64 NSIS installer, per-user, no admin).

All six platform archives, including the Windows ARM64 build (Microsoft Build of OpenJDK 17, native LWJGL2), are described in `docs/platforms.md` and produced by `scripts/build-all.sh`.

It installs to `%LOCALAPPDATA%\Programs\Confu Client`:

- `CONFUSION3H-launcher.jar`, `client\CONFUSION3H-Release.jar` (file names inside the install folder)
- `jre\` — Temurin 8 JRE x64 (sha256-pinned in the script); shortcuts run `jre\bin\javaw.exe`
- Start menu folder **Confu Client** (launcher, Music folder, Uninstall) and a Desktop shortcut with the C icon
- Uninstaller + "Apps & features" entry (HKCU), display name **Confu Client**

and creates `%APPDATA%\.confu\minecraft\glide\music` with a README only (no tracks are
bundled); existing files there are never overwritten.
If `%APPDATA%\.CONFUSION3H` is present and `.confu` is not, the installer renames
the old folder first, so worlds and settings move with it. The launcher does the
same rename on startup (Windows, Linux and macOS).

On Windows the launcher keeps its settings and game dir in `%APPDATA%\.confu`
and finds the client jar in `client\` next to itself. Uninstalling removes the program
only; worlds, settings, music and capes in `%APPDATA%\.confu` stay.

Silent install / uninstall: `Setup.exe /S`, `Uninstall.exe /S`.

The installer carries a VERSIONINFO block (product Confu Client, company Confu Client, original filename, file description) and a manifest that is DPI-aware and declares the supported Windows versions. The stub is compressed with NSIS LZMA solid. It is not packed with UPX. That is the useful metadata an unsigned installer can carry. Windows SmartScreen still shows "Windows protected your PC" / unrecognized app, because there is no Authenticode signature. On that dialog choose **More info**, then **Run anyway**.

Signing is optional and skipped when no certificate is configured. `scripts/sign-windows.sh` runs after `makensis` for both Windows archives:

- PKCS#12: `CONFU_SIGN_PFX` and `CONFU_SIGN_PASSWORD` (optional `CONFU_SIGN_TIMESTAMP`). Uses `osslsigncode` or `signtool`.
- Azure Trusted Signing: `CONFU_AZURE_DLIB` and `CONFU_AZURE_METADATA`, plus `AZURE_TENANT_ID`, `AZURE_CLIENT_ID` and `AZURE_CLIENT_SECRET` for the dlib. Uses `signtool`.

If one of those variables is set and the tool is missing, the build stops instead of publishing an unsigned file by mistake.

The x64 installer runs on Windows ARM under x64 emulation and says so at startup. The native ARM64 installer is `Confu-Client-Setup-<version>-win-arm64.exe` (see `docs/platforms.md`). It refuses to install on an x64 machine.
