# Platforms

Confu Client 0.1.0 ships six archives under `releases/`. Each one bundles a JRE.
The game itself is still Minecraft 1.8.9 (LWJGL 2.9.4, Mixin 0.7.11, LaunchWrapper 1.12).

| Archive | JRE | LWJGL natives | How it was checked |
|---|---|---|---|
| `Confu-Client-Setup-0.1.0-win-x64.exe` | Temurin 8 x64 | Mojang x64 | NSIS x64 installer, PE32+ |
| `Confu-Client-Setup-0.1.0-win-arm64.exe` | Microsoft Build of OpenJDK 17 aarch64 | HMCL LWJGL2 2.9.3-rc1 windows-arm64 | installer is an x64 stub (runs under emulation); payload JRE and DLLs are aarch64 |
| `Confu-Client-0.1.0-mac-x64.zip` | Temurin 8 x64 | Mojang x64 | `.app` zip, Mach-O x86_64 |
| `Confu-Client-0.1.0-mac-aarch64.zip` | Azul Zulu 8 aarch64 | HMCL LWJGL2 2.9.3-rc1 osx-arm64 | `.app` zip, Mach-O arm64 |
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

## JVM flags

Every launch uses G1 (`MaxGCPauseMillis=50`, a larger young generation, no explicit GC, biased locking off) plus `AlwaysPreTouch`, `UseStringDeduplication`, `PerfDisableSharedMem` and a 256 MB code cache. Those last four do not change what is drawn. On a software GL renderer the steady-state average barely moves; they cut allocation and pause jitter. The FPS preset (fast graphics, 8 chunks, minimum smooth lighting, VBOs, fewer particles, no entity shadows, vsync off) is unchanged. Chunk-update delay, "remove bots", and entity culling stay off: the first adds pop-in, the other two can hide real players.

## Run

- Windows: the installer. Start menu **Confu Client**.
- Linux: unpack and `./confu`.
- macOS: unzip, open `Confu Client.app`. Gatekeeper may require a right-click Open the first time (the app is not notarised).
