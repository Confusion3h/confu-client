# Confu Client

<p align="center"><img src="branding/png/mark-256.png" width="128" alt="Confu Client"></p>

Confu Client is a Minecraft 1.8.9 client. It is a GPL-3.0 fork of
[Glide Client](https://github.com/GlideClient/client), which is based on
[Soar Client](https://github.com/Soar-Client/Legacy-SoarClient) by EldoDebug.
The download page lives in `site/`.

## Features

- **FPS mode**, on from the first launch. Fast graphics, fewer particles, VBOs, and a tuned JVM. Chunk delay, bot removal and entity culling stay off, so real players stay on screen.
- **Music player.** Local WAV, MP3 and OGG, a YouTube or SoundCloud link (yt-dlp and FFmpeg download themselves and are checksum-checked), and optional Spotify control.
- **Microsoft sign-in** through the built-in Live client. No app to register.
- **Mod menu** from the Soar and Glide line: HUD modules, cosmetics and local capes.
- **Six archives**, each with a JRE: Windows x64, Windows ARM64 (native Java 17), macOS Intel, macOS Apple Silicon, Linux x64, Linux ARM64. Details are in `docs/platforms.md`.

## Build

The client compiles with JDK 8.

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-8-jdk-amd64
./gradlew build -x getAssets
```

`scripts/build-all.sh` builds every archive into `releases/`. It also needs NSIS (`makensis`), ImageMagick (`convert`), ffmpeg, curl and unzip. The Windows x64 installer alone is `scripts/build-windows-installer.sh`.

The download page:

```bash
cd site
npm install
npm run dev -- -p 43123
```

Open http://127.0.0.1:43123.

## Credits

- **Confu Client**: the rebrand, FPS mode, music links, launcher and the mark.
- **[Glide Client](https://github.com/GlideClient/client)**: the upstream fork this tree starts from.
- **[Soar Client](https://github.com/Soar-Client/Legacy-SoarClient)** by **EldoDebug**: the original client Glide and Confu Client build on.
- Inter (SIL OFL 1.1). The licence files ship with the font.

Licensed under GPL-3.0. See `LICENSE`. Java packages keep the upstream name `me.eldodebug.soar`.

Branding notes: `docs/branding.md`. Launcher: `launcher/README.md`.
