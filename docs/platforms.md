# Platforms

Confu Client 0.1.0 ships six archives under `releases/`. Each one bundles a JRE.
The game itself is still Minecraft 1.8.9 (LWJGL 2.9.4, Mixin 0.7.11, LaunchWrapper 1.12).

| Archive | JRE | LWJGL natives | How it was checked |
|---|---|---|---|
| `Confu-Client-Setup-0.1.0-win-x64.exe` | Temurin 8 x64 | Mojang x64 | NSIS x64 installer, PE32+ |
| `Confu-Client-Setup-0.1.0-win-arm64.exe` | Microsoft Build of OpenJDK 17 aarch64 | HMCL LWJGL2 2.9.3-rc1 windows-arm64 | installer is an x64 stub (runs under emulation); payload JRE and DLLs are aarch64 |
| `Confu-Client-0.1.0-mac-x64.zip` | Temurin 8 x64 | Mojang x64 | `.app` zip, Mach-O x86_64, needs macOS 11 or later |
| `Confu-Client-0.1.0-mac-aarch64.zip` | Azul Zulu 8 aarch64 | HMCL LWJGL2 2.9.3-rc1 osx-arm64 | `.app` zip, Mach-O arm64, needs macOS 12 or later (`openal.dylib`) |
| `Confu-Client-0.1.0-linux-x64.tar.gz` | Temurin 8 x64 | Mojang x64 | unpacked and launched |
| `Confu-Client-0.1.0-linux-aarch64.tar.gz` | Temurin 8 aarch64 | HMCL LWJGL2 2.9.3-linux-arm64 | ELF aarch64 structure only (this build machine is x64) |

Build them all with `scripts/build-all.sh`. The Windows x64 installer alone is `scripts/build-windows-installer.sh`.

## Windows ARM64

There is no Temurin 8 build for Windows aarch64. Java 11 and Java 17 can run this client. That was measured on Linux x64 OpenJDK 11.0.32 and 17.0.20 (same class libraries as the Microsoft builds), not by executing the Windows ARM DLLs:

- Stock LaunchWrapper 1.12 throws `ClassCastException` (`AppClassLoader` is not a `URLClassLoader`) before the game starts.
- After a small `launch-compat.jar` (classpath URLs taken from `java.class.path`, `com.sun.` and `jdk.` left to the app loader) Mixin 0.7.11 still cannot parse `java.lang.Thread` from a Java 11+ JDK, because its ASM is version 5 (class-file 52). The package ships Temurin 8 `rt.jar` as `java8-rt.jar` and Mixin reads those bytes.
- `javax.annotation-api` 1.3.2 is on the classpath (`javax.annotation.Resource` left the JDK in 11).
- The game JVM gets `--add-modules jdk.httpserver` (Spotify's loopback) and `--add-opens` for the modules Mixin and LWJGL reflect into.
- With that, both Java 11 and Java 17 reached the main menu (user set, LWJGL 2.9.4, sound engine, FPS mods) and stayed there. Java 8 with the same compat jar still does.

The ARM installer therefore bundles Microsoft Build of OpenJDK 17.0.20.1 Windows aarch64, `launch-compat.jar`, `java8-rt.jar`, and the HMCL natives `lwjgl64.dll` and `OpenAL64.dll` (both PE32+ Aarch64, LWJGL 2 JNI names). `jmods`, `lib/src.zip` and `lib/ct.sym` are left out: the VM runs from `lib/modules`, and those three files are only used to link or compile. The HMCL jar has no jinput DLLs, so controllers may fail to initialise; keyboard and mouse do not use jinput. The installer stub is x64 NSIS and only proceeds when the machine is native ARM64 (`IsNativeARM64`). The x64 installer still runs on ARM under emulation and says so.

`java8-rt.jar` is the Temurin 8 runtime class archive (GPL-2.0 with the Classpath Exception). It is metadata for Mixin. The process runs on the bundled Java 17 VM.

## macOS ARM64 and Linux ARM64

Those JREs are Java 8, so they do not need `java8-rt.jar`. After Mojang's natives are extracted, the launcher copies the matching HMCL directory (`natives/osx-arm64` or `natives/linux-arm64`) over them. On Linux the HMCL jar also contains LWJGL 3 leftovers (`libglfw.so`, `liblwjgl_opengl.so`, `liblwjgl_stb.so`, `libjemalloc.so`); the package keeps `liblwjgl.so` (also as `liblwjgl64.so`), `libopenal.so` (also as `libopenal64.so`) and `libjinput.so` (as `libjinput-linux.so` and `libjinput-linux64.so`).

## macOS

Two archives: Intel (`mac-x64`) and Apple Silicon (`mac-aarch64`). Each is a `Confu Client.app` bundle.

**First open (Gatekeeper).** The app is ad-hoc signed with rcodesign, not with an Apple Developer ID, and it is not notarised. A downloaded app carries the quarantine flag, so Gatekeeper says it cannot verify the app is free of malware. Once per Mac:

1. Open the app once and dismiss the message.
2. macOS 15 (Sequoia) and later: open **System Settings > Privacy & Security**, scroll to **Security**, click **Open Anyway** next to Confu Client and confirm. Control-click > **Open** no longer offers this on macOS 15 and later.
3. Earlier macOS: right-click (Control-click) **Confu Client.app** and choose **Open**.
4. Or, in Terminal: `xattr -dr com.apple.quarantine "/path/Confu Client.app"`, then open the app.

Removing the warning for everyone needs an Apple Developer ID Application certificate (Apple Developer Program, $99 a year) and notarisation with `notarytool`. Nothing in this build can clear the quarantine flag on a user's Mac, and an ad-hoc signature does not satisfy Gatekeeper.

**Signing.** Every Mach-O file is ad-hoc signed with rcodesign, with no hardened runtime and no entitlements. The bundle is not sealed: rcodesign 0.29 seals only a bundle whose main executable is a Mach-O, and `Contents/MacOS/confu` is a `/bin/sh` script. `scripts/macos/sign-app.py verify` checks the Mach-O files, the bundle keys, the symlinks and the icon.

**Launch path.** `Contents/MacOS/confu` (a `/bin/sh` script) runs `Contents/MacOS/confu-java`, a symlink to `Resources/jre/bin/java`. Because the JRE runs from inside `Contents/MacOS`, macOS applies the bundle's `Info.plist`: `NSHighResolutionCapable` for a sharp launcher on Retina, and `CFBundleIconFile` (`confusion3h.icns`, 16 to 1024 px, the same monochrome mark as the Windows icon). `Contents/lib` is a symlink to `Resources/jre/lib` so the java launcher finds `libjli` from that path. The Dock icon is set twice: by `-Xdock:icon` and by `setDockIconImage` once the launcher window opens.

**Sign-in.** Microsoft sign-in uses the device code flow in the default browser (see [Microsoft account login](microsoft-login.md)). The launcher reads no browser and asks for no Automation permission to a browser.

**Game window.** The game JVM starts like vanilla 1.8.9 on macOS: no `-XstartOnFirstThread` and no AWT. An app started by another app is not active, and since macOS 14 a window it creates can open behind the active app. So the launcher hides itself while the game runs, then asks System Events to bring the game process to the front, retrying for a few seconds. macOS asks once whether Confu Client may control System Events; that prompt uses the `NSAppleEventsUsageDescription` text in the bundle. If you deny it, the game window may stay behind other windows: use Cmd-Tab or click it. Clicking the Confu Client Dock icon brings the launcher back.

**Not verified in this build.** The build ran on Linux without a Mac. Still to check on hardware: that the game window appears and takes focus on launch, that the launcher is sharp on Retina, that the Dock icon shows the Confu mark, the System Events prompt, and the device-code page in Safari.

## JVM flags

Every launch uses G1 (`MaxGCPauseMillis=50`, a larger young generation, no explicit GC, biased locking off) plus `AlwaysPreTouch`, `UseStringDeduplication`, `PerfDisableSharedMem` and a 256 MB code cache. Those last four do not change what is drawn. On a software GL renderer the steady-state average barely moves; they cut allocation and pause jitter. The FPS preset (fast graphics, 8 chunks, minimum smooth lighting, VBOs, fewer particles, no entity shadows, vsync off) is unchanged. Chunk-update delay, "remove bots", and entity culling stay off: the first adds pop-in, the other two can hide real players.

## Run

- Windows: the installer. Start menu **Confu Client**.
- Linux: unpack and `./confu`.
- macOS: unzip anywhere, including `~/Downloads`, and open `Confu Client.app`. The first open needs one approval from macOS; see **macOS** below.

The repeated `[Client thread/ERROR] The jar file …/lwjgl/2.9.2-nightly-…` lines are LaunchWrapper's seal check. The 2.9.2 nightly and `lwjgl_util` both mark `org.lwjgl` sealed, and the jars are unsigned, so every class logs the mismatch and is still defined. The game reaches the texture atlas with those lines present. The `[main/ERROR] Classloader restrictions [PACKAGE_CLASSLOADER_EXCLUSION]` lines are Mixin 0.7 noting that mixin classes are excluded from the launch class loader, which is how Mixin keeps from transforming itself. Mixins still apply. Neither line is the missing window.
