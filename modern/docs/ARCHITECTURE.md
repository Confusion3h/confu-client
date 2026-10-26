# Confu Client Modern: architecture

This branch (`modern-1.20.1`) adds a Minecraft 1.20.1 client built on Fabric. It lives entirely
in `modern/` plus minimal launcher changes. The 1.8.9 client (`src/`, root `build.gradle`) is
not changed.

## Targets

| Item | Version | Source |
|---|---|---|
| Minecraft | 1.20.1 (Java 17) | Mojang piston-meta |
| Mappings | Yarn 1.20.1+build.10 | maven.fabricmc.net |
| Loader | Fabric Loader 0.20.1 (stable for 1.20.1) | meta.fabricmc.net |
| API | Fabric API 0.92.12+1.20.1 | maven.fabricmc.net |
| Build | Gradle 8.8 wrapper, Fabric Loom 1.5.8 | maven.fabricmc.net, services.gradle.org |
| Java | toolchain 17 (compile and run) | adoptium.net |

Yarn names are used in mixins. Each mixin target is checked by compiling and launching the game,
because Loom remaps mixins at build time and a wrong name fails at launch.

## Repository layout

```
modern/
  build.gradle, settings.gradle, gradle.properties, gradlew(.bat), gradle/wrapper/
  README.md
  docs/
    INVENTORY.md          1.8.9 module list and port status
    ARCHITECTURE.md       this file
    VERIFICATION.md       per-phase results and screenshots
  scripts/
    xvfb-run.sh           starts Xvfb, the game or launcher, and takes screenshots
  src/main/java/me/eldodebug/confu/
    ConfuClient.java      ClientModInitializer: wires config, modules, keybinds and events
    config/               JSON settings file (config/confu/config.json)
    lang/                 EN and IT string tables
    module/               Module base class, ModuleManager, categories
    module/setting/       BooleanSetting, NumberSetting, ComboSetting, KeybindSetting
    module/impl/          one class per ported module
    hud/                  HUD element base class, drag position, layout
    render/               rounded rectangles and gradients on DrawContext
    gui/                  mod menu screen, widgets and pages
    cape/                 cape registry, texture loading, player cape override
    mixin/                mixin classes (see below)
  src/main/resources/
    fabric.mod.json
    confu.mixins.json
    assets/confu/lang/en_us.json, it_it.json
    assets/confu/capes/minecon/..., flag/..., custom/...
    assets/confu/textures/        crosshair sheet, logo, background
```

Package naming: `me.eldodebug.confu`. The 1.8.9 tree keeps `me.eldodebug.soar` (upstream name, see README).

## Module model

Each 1.8.9 `Mod` maps to a `Module`:

- `Module` has an id (the 1.8.9 `alias`, lowercased), a name key and description key, a category
  (`ALL`, `PLAYER`, `RENDER`, `HUD`, `WORLD`, `OTHER` to match the 1.8.9 tabs), an enabled flag, a
  keybind and a list of settings.
- `onEnable()` and `onDisable()` run on toggle. Per-frame work runs from the events below, not in
  the module constructor.
- `ModuleManager` owns the list in registration order, saves and loads state, and exposes lookups
  by id for mixins (`ModuleManager.get(Zoom.class)`).
- Settings are typed: `BooleanSetting`, `NumberSetting` (min, max, step, integer flag),
  `ComboSetting` (option list), `KeybindSetting` (GLFW key code, or -1 for none). Each setting has
  a stable key used in the JSON file.

HUD modules extend `HudElement`. Position is stored as a fraction of the screen so it survives a
resize. `HudElement.render(DrawContext, delta)` draws the element. Drag handling lives in the menu's
HUD editor page.

## Events and hooks

Only Fabric API events and a small set of mixins are used. No custom event bus.

| Need | Mechanism |
|---|---|
| Per-tick logic (zoom, freelook state, CPS window) | `ClientTickEvents.END_CLIENT_TICK` |
| HUD drawing | `HudRenderCallback` (Fabric API) |
| Keybinds | `KeyBindingHelper.registerKeyBinding` |
| Mod menu | `KeyBinding` default RSHIFT (1.8.9 default) |
| Mouse button presses (CPS) | mixin into `Mouse.onMouseButton` (HEAD) |
| Zoom FOV | mixin into `GameRenderer.getFov` (RETURN, `setReturnValue`) |
| Freelook camera angles | mixin into `Camera.update` (`@ModifyArgs` on the `setRotation` call) |
| Freelook mouse capture | mixin into `Entity.changeLookDirection` (HEAD, cancellable, client player only) |
| Hide vanilla crosshair | mixin into `InGameHud.renderCrosshair` (HEAD, cancellable) |
| Cape texture | mixin into the client player's cape texture accessor |

Mixin rule: never touch chunk rendering, `WorldRenderer` chunk builders or the render pipeline
that Sodium replaces. Sodium-safe hooks are the ones above. Each mixin is listed in
`confu.mixins.json` and in the table in `docs/VERIFICATION.md` with the Sodium check result.

## Rendering

- Panels, toggles and cards: `render/Draw.java` draws rounded rectangles as stacked
  `DrawContext.fill` rows, so it needs no shader and no custom vertex format. Radius is small
  (2 to 8 px), so the cost per frame is a few dozen calls per card.
- Text: vanilla `TextRenderer`. An Inter font provider is a later step.
- Crosshair preview in the menu draws the same sprite sheet the HUD uses, so the preview and the
  in-game crosshair cannot drift apart.
- Colours come from one palette in `render/Theme.java`, using the Confu green accent from the
  mark and the dark panels from the website mockup.

## Mod menu (same look as 1.8.9)

Layout taken from the 1.8.9 mod menu (`website/media/client-menu.png`):

- Centred dark panel, left icon rail, one page at a time.
- Pages: Home (quick toggles, tips), Modules (tabs ALL, PLAYER, RENDER, HUD, WORLD, OTHER with
  cards, toggles and settings), Cosmetics (capes grid with live preview), Settings (language,
  menu keybind, HUD editor, about and credits).
- Rail icons and the mark come from `branding/png/`. Icons are simple vector shapes so no icon font
  is needed.

## Languages

`assets/confu/lang/en_us.json` and `it_it.json` hold every string. The active language comes from the
Settings page. The default follows the Minecraft language (`it_it` gives Italian, anything else
English). Keys use the 1.8.9 `text.*` names where the feature exists, so the two translation sets
stay easy to compare.

## Cosmetics and capes

- Cape sources: the 1.8.9 PNG files, copied under `assets/confu/capes/`, in the categories MINECON
  (5), FLAG (19) and CUSTOM (user files in `<gameDir>/confu/capes/`, validated as 64x32 PNG).
  SOAR, CARTOON and MISC are not shipped.
- Loading: PNG files are read as `NativeImage`, registered as `DynamicTexture`s on the client
  thread, and reused across menu opens.
- Rendering: only the local player gets the selected cape in phase 4. Remote players need a server
  or relay, which 1.8.9 Confu also does not have for these capes.

## Music player

Phase 4 scope: local WAV and OGG via `javax.sound.sampled` and JOrbis (OGG is in Minecraft's own
libraries), and MP3 via `javazoom:jlayer` (LGPL-2.1, the same decoder 1.8.9 Confu uses). Link
playback (yt-dlp, FFmpeg) and Spotify are later. The player runs on its own thread and never touches
the render thread except for text.

## Launcher integration (phase 3)

Minimal changes to `launcher/`. The 1.8.9 path keeps working unchanged.

- Version selector on the home page: `1.8.9` (default) and `1.20.1 (Modern)`. The choice is saved in
  `launcher.properties` as `game.version`.
- Modern install, Java 8 code: read the Mojang version JSON for 1.20.1, download the client jar,
  libraries (LWJGL 3 natives extracted per OS), assets index and objects with the same SHA-1 checks
  the 1.8.9 path uses.
- Fabric: the loader profile for 1.20.1 from `meta.fabricmc.net/v2/versions/loader/1.20.1/<loader>/profile/json`.
  Its main class is `net.fabricmc.loader.impl.launch.knot.KnotClient`. Its libraries use their own
  Maven URLs.
- Mods: the Fabric API jar and the Confu modern jar go into `<gameDir>/mods/`. The Confu jar is
  found in `modern/build/libs/` next to the repo, or next to the launcher jar, or set with
  `-Dconfusione.modern=...`.
- Java: Minecraft 1.20.1 needs Java 17. The launcher uses a system Java 17+ when one is found,
  otherwise it downloads Eclipse Temurin 17 JRE from the Adoptium API into `~/.confu/runtime/`.
- Login: offline uses the offline UUID with `--userType legacy`, as today. Microsoft uses the same
  refresh-token and Minecraft-token path as 1.8.9 (`MicrosoftAuth`) with `--userType msa`. The
  Minecraft access token is the same for 1.20.1, so no new sign-in code is needed.

## Build, run, and verify

```bash
cd modern
./gradlew build                  # build/libs/confu-modern-<version>.jar
./gradlew runClient              # development run with Loom
scripts/xvfb-run.sh shot <name>  # headless run under Xvfb, writes docs/screens/<name>.png
```

## Phases

1. Inventory and architecture (this file, `INVENTORY.md`).
2. Fabric skeleton: Gradle build, mod menu, Zoom, Freelook, Crosshair, Keystrokes, EN and IT.
   Verified by a Xvfb run to the main menu and the mod menu.
3. Launcher version selector. Launching `1.20.1 (Modern)` installs Fabric and reaches the main
   menu under Xvfb.
4. Remaining mods that fit the scope: CPS, FPS, Ping, Armor status, Clock, Coords, capes (MINECON,
   FLAG, CUSTOM), local music player. Each one has an in-game screenshot or a clear note that it
   was not verified.

## Known limits

- Fabric Loader 0.20.1 and Java 17 are the first combination tried. A different loader is used only
  if it fails at launch, and the reason is recorded.
- Microsoft sign-in is not verified end-to-end without a Microsoft account. The code path is the
  same as 1.8.9. Offline and the Microsoft code path are both checked for the command line.
- Sodium is not bundled. It is tested as an optional jar in `mods/` when a Sodium build for 1.20.1
  is available from the Modrinth API.
