# CONFUSION3H launcher

Standalone Java 8 launcher for the CONFUSION3H client (Minecraft 1.8.9). No dependencies:
Swing/Java2D UI, JDK HTTPS, a tiny JSON reader.

    ./gradlew :launcher:build            # -> launcher/build/libs/CONFUSION3H-launcher-0.1.0.jar
    java -jar launcher/build/libs/CONFUSION3H-launcher-0.1.0.jar

## What it does
1. Finds the client jar: Settings path, `-Dconfusione.client=...`, or the newest jar in
   `build/libs` of the client repo (searched upward from the working dir and the launcher jar).
   If the repo is found but not built, the home screen offers to run `gradlew build`.
2. Reads the tweak class from the client jar manifest (`TweakClass`, falls back to
   `me.eldodebug.soar.injection.mixin.GlideTweaker`).
3. Installs vanilla 1.8.9 into the game dir (default `~/.CONFUSION3H/minecraft`) from
   Mojang's HTTPS metadata: version JSON, client jar, libraries (OS rules applied),
   natives (extracted to `versions/1.8.9/natives`), asset index + objects. Everything is
   SHA-1 verified; already-present files are skipped. Adds `launchwrapper-1.12` and
   `asm-all-5.0.3`, which vanilla 1.8.9 lacks.
4. Runs `net.minecraft.launchwrapper.Launch --tweakClass <tweaker>` with the client jar
   first on the classpath, offline session (`--userType legacy`, offline UUID).

## CLI
    --setup-only        download/verify game files only
    --dry-run           setup, then print the full launch command and classpath check
    --launch            setup and launch without the UI
    --screenshot F.png [--page home|settings|console] [--progress 0.4]
    --gamedir DIR  --username NAME  --client JAR      (one-off overrides)

Settings live in `~/.CONFUSION3H/launcher.properties`.
