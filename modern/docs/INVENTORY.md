# Confu Client 1.8.9 inventory and 1.20.1 port plan

Source of truth: `src/main/java/me/eldodebug/soar/management/mods/ModManager.java` on `main`
(121 `mods.add(...)` entries), plus the GUI, cape, language and music packages.
The 1.8.9 tree is not touched by this branch.

## Status legend

| Status | Meaning |
|---|---|
| Phase 2 | Implemented in the Fabric skeleton phase (core set). |
| Phase 4 | Planned for the remaining-mods phase. |
| Later | Feasible on Fabric 1.20.1, not in the current scope. |
| Not planned | Dropped on purpose: 1.8.9-only mechanic, anti-feature, automation, or conflicts with the Sodium-friendly rendering rule. |

Category names are the five 1.8.9 `ModCategory` values used as tabs in the module menu:
ALL, PLAYER, RENDER, HUD, WORLD, OTHER.

Type: `Mod` is a plain module, `HUD` is `HUDMod` (draggable element), `SimpleHUD` is `SimpleHUDMod`
(one line of text plus an optional icon).

## Modules (121)

| # | 1.8.9 class | Category | Type | Status | Notes for 1.20.1 |
|---|---|---|---|---|---|
| 1 | AnimationsMod | RENDER | Mod | Later | 1.8 block-hit and swing animation; needs a first-person arm render hook. |
| 2 | AppleSkinMod | PLAYER | Mod | Later | Hunger and saturation overlay on the food bar (InGameHud mixin). |
| 3 | ArmorStatusMod | HUD | HUD | Phase 4 | Durability bars for worn armour, horizontal or vertical. |
| 4 | ArrayListMod | HUD | HUD | Later | Enabled-module list; trivial once the module registry exists. |
| 5 | AsyncScreenshotMod | OTHER | Mod | Later | Off-thread PNG write. Vanilla screenshots are synchronous. |
| 6 | AutoTextMod | PLAYER | Mod | Later | Keybind sends a preset chat line. |
| 7 | BlockInfoMod | HUD | HUD | Later | Name of the looked-at block. |
| 8 | BlockOverlayMod | RENDER | Mod | Later | Block outline restyle (drawBlockOutline hook). |
| 9 | BloodParticlesMod | RENDER | Mod | Later | Particles on hit; particle manager hook. |
| 10 | BorderlessFullscreenMod | OTHER | Mod | Later | GLFW window hint. Vanilla fullscreen exists. |
| 11 | BossHealthMod | HUD | HUD | Later | Boss bar position and scale. |
| 12 | BowZoomMod | PLAYER | Mod | Later | Reuses the Zoom FOV hook. |
| 13 | BreadcrumbsMod | RENDER | Mod | Later | Path trail rendering. |
| 14 | CalendarMod | HUD | HUD | Later | Date text. |
| 15 | ChatMod | OTHER | Mod | Later | Chat smooth scrolling and animation (ChatHud mixin). |
| 16 | ChatTranslateMod | OTHER | Mod | Not planned | Needs an external translation service and sends chat text off the machine. |
| 17 | ChunkAnimatorMod | RENDER | Mod | Not planned | Hooks chunk rebuilds; conflicts with Sodium's chunk pipeline. |
| 18 | ChunkBordersMod | RENDER | Mod | Later | Debug chunk borders; draw from a world render event, not a chunk mixin. |
| 19 | ClearGlassMod | RENDER | Mod | Later | Glass transparency; texture override. |
| 20 | ClearWaterMod | RENDER | Mod | Later | Water fog and texture (fog mixin). |
| 21 | ClientSpooferMod | OTHER | Mod | Not planned | Client brand spoofing; an anti-feature. |
| 22 | ClockMod | SimpleHUD | SimpleHUD | Phase 4 | Plain text. |
| 23 | ColorSaturationMod | RENDER | Mod | Later | Post-process shader; needs a post-effect that Sodium leaves alone. |
| 24 | ComboCounterMod | SimpleHUD | SimpleHUD | Later | Hit combo counter (attack event). |
| 25 | CompassMod | SimpleHUD | SimpleHUD | Later | Compass strip. |
| 26 | CoordsMod | SimpleHUD | SimpleHUD | Phase 4 | Player position text. |
| 27 | CPSDisplayMod | SimpleHUD | SimpleHUD | Phase 4 | Left and right clicks per second from a GLFW mouse-button hook. |
| 28 | CrosshairMod | RENDER | Mod | Phase 2 | Custom 9-style sprite sheet, scale, hide in third person, live preview in the menu. |
| 29 | CustomHeldItemsMod | RENDER | Mod | Later | Held item model override. |
| 30 | DamageParticlesMod | RENDER | Mod | Later | Damage particles. |
| 31 | DamageTiltMod | PLAYER | Mod | Later | Hurt camera tilt (tiltViewWhenHurt hook). |
| 32 | DamageTintMod | RENDER | Mod | Later | Red overlay on damage. |
| 33 | DayCounterMod | SimpleHUD | SimpleHUD | Later | World day count. |
| 34 | DiscordRPCMod | OTHER | Mod | Later | Discord IPC; needs a pipe client. |
| 35 | EarsMod | PLAYER | Mod | Later | Cosmetic ears (textures exist in the 1.8.9 resources); needs a player model layer. |
| 36 | EntityCullingMod | OTHER | Mod | Not planned | Can hide real players; Sodium and vanilla already cull. |
| 37 | FarCameraMod | RENDER | Mod | Later | Third-person camera distance (clipToSpace hook). |
| 38 | FemaleGenderMod | PLAYER | Mod | Later | Slim model; needs an alternate player model. |
| 39 | FovModifierMod | PLAYER | Mod | Later | FOV slider; reuses the Zoom FOV hook. |
| 40 | FPSBoostMod | OTHER | Mod | Not planned | Chunk delay, bot removal, entity culling. The 1.8.9 build keeps all three off, so the mod adds nothing; Sodium covers performance. |
| 41 | FPSDisplayMod | SimpleHUD | SimpleHUD | Phase 4 | `getCurrentFps()`. |
| 42 | FPSLimiterMod | OTHER | Mod | Later | Vanilla already has a framerate cap option. |
| 43 | FPSSpooferMod | OTHER | Mod | Not planned | FPS spoofing; an anti-feature. |
| 44 | FreelookMod | PLAYER | Mod | Phase 2 | Freelook with a smooth transition in and out. |
| 45 | FullbrightMod | PLAYER | Mod | Later | Gamma override (lightmap hook). |
| 46 | GlintColorMod | RENDER | Mod | Later | Enchantment glint colour. |
| 47 | InternalSettingsMod | OTHER | Mod | Phase 2 | Theme, language and menu key live in the Settings page. |
| 48 | GodbridgeAssistMod | HUD | HUD | Not planned | PvP automation assist. |
| 49 | HealthDisplayMod | SimpleHUD | SimpleHUD | Later | Health text. |
| 50 | HitBoxMod | RENDER | Mod | Later | Hitbox render. |
| 51 | HitColorMod | RENDER | Mod | Later | Hurt flash colour. |
| 52 | HitDelayFixMod | PLAYER | Mod | Not planned | 1.8.9 PvP hit-delay compatibility; no equivalent in 1.20.1. |
| 53 | HorseStatsMod | HUD | HUD | Later | Horse stats text. |
| 54 | HypixelMod | OTHER | Mod | Not planned | Server-specific (AutoGG and similar). |
| 55 | HypixelQuickPlayMod | PLAYER | Mod | Not planned | Server-specific queue commands. |
| 56 | ImageDisplayMod | HUD | HUD | Later | Image overlay. |
| 57 | InventoryDisplayMod | HUD | HUD | Later | Inventory mirror on the HUD. |
| 58 | InventoryMod | OTHER | Mod | Later | Inventory animation and layout tweaks. |
| 59 | ItemInfoMod | RENDER | Mod | Later | Tooltip colour info (potion colour). |
| 60 | ItemPhysicsMod | RENDER | Mod | Later | Dropped item physics (item entity renderer). |
| 61 | Items2DMod | RENDER | Mod | Later | Flat item rendering. |
| 62 | JumpCircleMod | RENDER | Mod | Later | Jump ring effect. |
| 63 | KeystrokesMod | HUD | HUD | Phase 2 | WASD, Space, mouse buttons; press animation; unmarked style. |
| 64 | KillEffectsMod | RENDER | Mod | Later | Kill effect particles. |
| 65 | KillSoundsMod | OTHER | Mod | Later | Sound on kill (event). |
| 66 | MechvibesMod | OTHER | Mod | Later | Keyboard sound packs (WAV assets exist). |
| 67 | MemoryUsageMod | SimpleHUD | SimpleHUD | Later | Heap usage text. |
| 68 | MinemenMod | OTHER | Mod | Not planned | Auto-play automation. |
| 69 | MinimalDamageShakeMod | RENDER | Mod | Later | Reduced hurt shake. |
| 70 | MinimalViewBobbingMod | RENDER | Mod | Later | Reduced view bobbing. |
| 71 | MinimapMod | HUD | HUD | Later | Chunk-atlas minimap; large render subsystem. |
| 72 | MoBendsMod | PLAYER | Mod | Later | Animated player model; large subsystem. |
| 73 | ModernHotbarMod | HUD | HUD | Later | Hotbar restyle. |
| 74 | MotionBlurMod | RENDER | Mod | Later | Post-process shader. |
| 75 | MouseStrokesMod | HUD | HUD | Later | Mouse-stroke overlay, same family as Keystrokes. |
| 76 | MusicPlayerMod | HUD | HUD | Phase 4 | Local WAV, OGG and MP3 files; link playback (yt-dlp) is later. |
| 77 | NameDisplayMod | SimpleHUD | SimpleHUD | Later | Player name text. |
| 78 | NameProtectMod | PLAYER | Mod | Later | Hide own name in HUD and chat. |
| 79 | NametagMod | PLAYER | Mod | Later | Nametag restyle. |
| 80 | OverlayEditorMod | RENDER | Mod | Later | Hide individual HUD overlays. |
| 81 | PackDisplayMod | HUD | HUD | Later | Resource pack list. |
| 82 | ParticleCustomizerMod | RENDER | Mod | Later | Particle settings. |
| 83 | PingDisplayMod | SimpleHUD | SimpleHUD | Phase 4 | Latency from the player list entry. |
| 84 | PlayerCounterMod | SimpleHUD | SimpleHUD | Later | Online player count. |
| 85 | PlayerDisplayMod | HUD | HUD | Later | 3D player preview. |
| 86 | PlayerListMod | HUD | HUD | Later | Tab list changes. |
| 87 | PlayerPredicatorMod | WORLD | Mod | Later | Player prediction helper. |
| 88 | PlayTimeDisplayMod | SimpleHUD | SimpleHUD | Later | Session play time. |
| 89 | PotionCounterMod | SimpleHUD | SimpleHUD | Later | Active potion count. |
| 90 | PotionStatusMod | HUD | HUD | Later | Potion list. |
| 91 | ProjectileTrailMod | PLAYER | Mod | Later | Projectile trail. |
| 92 | QuickSwitchMod | PLAYER | Mod | Later | Sword quick switch. |
| 93 | RawInputMod | OTHER | Mod | Later | Raw mouse input. Vanilla has a raw input option. |
| 94 | ReachCirclesMod | PLAYER | Mod | Not planned | Reach visualisation for 1.8 PvP; low value. |
| 95 | ReachDisplayMod | SimpleHUD | SimpleHUD | Later | Reach distance text. |
| 96 | RearviewMod | HUD | HUD | Later | Rear camera; needs a second render pass. |
| 97 | ScoreboardMod | HUD | HUD | Later | Scoreboard restyle. |
| 98 | ServerIPDisplayMod | SimpleHUD | SimpleHUD | Later | Server address text. |
| 99 | SessionInfoMod | HUD | HUD | Later | Session info panel. |
| 100 | ShinyPotsMod | RENDER | Mod | Later | Potion item shine. |
| 101 | Skin3DMod | RENDER | Mod | Later | 3D skin layers; large subsystem. |
| 102 | SkinProtectMod | PLAYER | Mod | Later | Replace own skin with the default. |
| 103 | SlowSwingMod | PLAYER | Mod | Later | Swing delay option. |
| 104 | SoundModifierMod | OTHER | Mod | Later | Per-category volume and pitch. |
| 105 | SoundSubtitlesMod | HUD | HUD | Later | Subtitles; vanilla subtitles exist. |
| 106 | SpeedometerMod | SimpleHUD | SimpleHUD | Later | Speed text. |
| 107 | StopwatchMod | SimpleHUD | SimpleHUD | Later | Stopwatch. |
| 108 | TabEditorMod | RENDER | Mod | Later | Tab list background. |
| 109 | TaplookMod | PLAYER | Mod | Later | Tap-to-look; variant of Freelook. |
| 110 | TargetIndicatorMod | RENDER | Mod | Later | Target indicator. |
| 111 | TargetInfoMod | HUD | HUD | Later | Target info panel. |
| 112 | TimeChangerMod | WORLD | Mod | Later | Client-side time override. |
| 113 | TNTTimerMod | SimpleHUD | SimpleHUD | Later | TNT fuse timer. |
| 114 | ToggleSneakMod | PLAYER | Mod | Later | Toggle sneak keybind. |
| 115 | ToggleSprintMod | SimpleHUD | SimpleHUD | Later | Toggle sprint keybind with indicator. |
| 116 | UHCOverlayMod | RENDER | Mod | Not planned | UHC-specific overlay. |
| 117 | WaveyCapesMod | RENDER | Mod | Later | Cape physics (StickSimulation). Static capes come first. |
| 118 | WaypointMod | WORLD | Mod | Later | Waypoints (the 1.8.9 waypoint GUI is also later). |
| 119 | WeatherChangerMod | WORLD | Mod | Later | Client-side weather override. |
| 120 | WeatherDisplayMod | SimpleHUD | SimpleHUD | Later | Weather text. |
| 121 | ZoomMod | PLAYER | Mod | Phase 2 | Smooth zoom, scroll zoom, zoom speed and factor. |

Counts: Phase 2 = 5 (Crosshair, Freelook, InternalSettings, Keystrokes, Zoom), Phase 4 = 7 (ArmorStatus, Clock, Coords, CPS, FPS, MusicPlayer, Ping), Not planned = 13, Later = 96. Total 121.

## Features that are not modules

| 1.8.9 feature | Source | 1.20.1 status |
|---|---|---|
| Mod menu, RSHIFT, categories Home, Modules, Cosmetics, Games, Profile, Screenshot, Settings | `gui/modmenu/` | Phase 2: Home, Modules, Cosmetics (capes), Settings. Games, Profile and Screenshot are Later. |
| Capes: MINECON (5), FLAG (19), SOAR (11), CARTOON (13), MISC (3), CUSTOM (import from folder) | `management/cape/CapeManager.java` | Phase 4: MINECON, FLAG and CUSTOM only. SOAR, CARTOON and MISC are excluded by request. |
| Language: EN, IT | `management/language/` | Phase 2: `en_us` and `it_it` JSON in `assets/confu/lang/`. 1.8.9 has no Italian, so Italian is new. |
| Main menu background and branding | `assets/minecraft/soar/mainmenu/`, `branding/` | Phase 2: Confu mark on the title screen. Full redesign is Later. |
| Notifications, commands (`.soarcmd`), waypoints GUI, profiles, quick play, games | `management/notification`, `management/command`, `quickplay`, `profile` | Later (notifications are cheap and may land in Phase 2). |
| Discord RPC, Mechvibes, music links (yt-dlp, Spotify) | `discord/`, `music/`, `management/mods/impl/MechvibesMod.java` | Later, except local music playback in Phase 4. |
| Security feature set (`management/security`) | `ResourcePackSecurity`, `Log4jSecurity`, `ExplosionSecurity`, `ParticleSecurity`, `TeleportSecurity`, `DemoSecurity` | Later. Log4j and resource-pack checks are vanilla in 1.20.1; review before porting. |
| Serverside blacklist (`RestrictedMod`) | `management/mods/RestrictedMod.java` | Not planned. It exists to restrict mods on some servers; a decision for the product owner. |
