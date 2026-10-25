# Confu Client launcher

Standalone Java 8 launcher for Confu Client (Minecraft 1.8.9). No dependencies:
Swing/Java2D UI, JDK HTTPS, a tiny JSON reader.

    ./gradlew :launcher:build            # -> launcher/build/libs/CONFUSION3H-launcher-0.1.0.jar
    java -jar launcher/build/libs/CONFUSION3H-launcher-0.1.0.jar

## What it does
1. Finds the client jar: Settings path, `-Dconfusione.client=...`, or the newest jar in
   `build/libs` of the client repo (searched upward from the working dir and the launcher jar).
   If the repo is found but not built, the home screen offers to run `gradlew build`.
2. Reads the tweak class from the client jar manifest (`TweakClass`, falls back to
   `me.eldodebug.soar.injection.mixin.GlideTweaker`).
3. Installs vanilla 1.8.9 into the game dir (default `~/.confu/minecraft`,
   `%APPDATA%\.confu\minecraft` on Windows) from Mojang's HTTPS metadata: version
   JSON, client jar, libraries (OS rules applied), natives (extracted to
   `versions/1.8.9/natives`), asset index + objects. Each file is written to a unique
   temp file, SHA-1 checked, then moved into place. Names that share a hash are fetched
   once. A library that still mismatches aborts setup; an asset that still mismatches
   after retries is logged and skipped. Adds `launchwrapper-1.12` and `asm-all-5.0.3`,
   which vanilla 1.8.9 lacks.
4. Runs `net.minecraft.launchwrapper.Launch --tweakClass <tweaker>` with the client jar
   first on the classpath. Offline (default): `--userType legacy` and an offline UUID.
   Microsoft: one click opens a sign-in window (no Azure app to register), then
   refresh on launch, `--userType msa` with the real profile. See
   `docs/microsoft-login.md`. The refresh token stays in `msa.properties`
   next to the settings file and is never committed.

## CLI
    --setup-only        download/verify game files only
    --dry-run           setup, then print the full launch command and classpath check
    --launch            setup and launch without the UI
    --screenshot F.png [--page home|settings|console] [--progress 0.4]
    --gamedir DIR  --username NAME  --client JAR      (one-off overrides)

Settings live in `~/.confu/launcher.properties` (`%APPDATA%\.confu` on Windows).
If `.CONFUSION3H` is already there and `.confu` is not, the launcher renames it on startup.
The older `~/.confusione` folder is still picked up the same way when it sits next to the new one.
Microsoft sign-in works without a client ID. The refresh token is stored separately.
