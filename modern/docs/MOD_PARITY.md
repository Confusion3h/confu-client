# MOD_PARITY — 1.8.9 → 1.20.1 Fabric

Source: `src/.../mods/ModManager.java` (121 mods). Target: `modern/` (Fabric 1.20.1).
Branch: `modern-allmods`. 1.8.9 tree (`src/`) is not modified.

## Status legend

| Status | Meaning |
|---|---|
| Ported | Present in `modern/` ModuleManager and builds. |
| Pending | Must be ported for parity. |
| Impossible | Cannot match 1.8.9 on 1.20.1; reason in Notes. |
| Modern-only | Exists only on 1.20.1; keep. |

## Modules (121 from 1.8.9)

| # | 1.8.9 class | Category | Type | Status | Notes |
|---|---|---|---|---|---|
| 1 | AnimationsMod | RENDER | Mod | Pending | 1.8 block-hit/swing; first-person transform mixin. |
| 2 | AppleSkinMod | PLAYER | Mod | Pending | Hunger/saturation overlay on food bar. |
| 3 | ArmorStatusMod | HUD | HUD | Ported | Worn armour icons, H/V layout. |
| 4 | ArrayListMod | HUD | HUD | Pending | Enabled-module list. |
| 5 | AsyncScreenshotMod | OTHER | Mod | Pending | Off-thread PNG write. |
| 6 | AutoTextMod | PLAYER | Mod | Pending | Keybind sends preset chat. |
| 7 | BlockInfoMod | HUD | HUD | Pending | Looked-at block name. |
| 8 | BlockOverlayMod | RENDER | Mod | Pending | Block outline restyle. |
| 9 | BloodParticlesMod | RENDER | Mod | Pending | Particles on hit. |
| 10 | BorderlessFullscreenMod | OTHER | Mod | Pending | GLFW borderless fullscreen. |
| 11 | BossHealthMod | HUD | HUD | Pending | Boss bar position/scale. |
| 12 | BowZoomMod | PLAYER | Mod | Pending | Bow FOV; reuses Zoom FOV hook. |
| 13 | BreadcrumbsMod | RENDER | Mod | Pending | Path trail. |
| 14 | CalendarMod | HUD | HUD | Pending | Month calendar widget. |
| 15 | ChatMod | OTHER | Mod | Pending | Chat scroll/animation. |
| 16 | ChatTranslateMod | OTHER | Mod | Pending | External translate API. |
| 17 | ChunkAnimatorMod | RENDER | Mod | Impossible | Needs chunk-rebuild hooks; conflicts with Sodium (project rule). |
| 18 | ChunkBordersMod | RENDER | Mod | Pending | World-render chunk borders. |
| 19 | ClearGlassMod | RENDER | Mod | Pending | Glass transparency. |
| 20 | ClearWaterMod | RENDER | Mod | Pending | Water fog/texture. |
| 21 | ClientSpooferMod | OTHER | Mod | Pending | Client brand spoof. |
| 22 | ClockMod | HUD | SimpleHUD | Ported | Clock text. |
| 23 | ColorSaturationMod | RENDER | Mod | Pending | Post-process saturation. |
| 24 | ComboCounterMod | HUD | SimpleHUD | Pending | Hit combo counter. |
| 25 | CompassMod | HUD | SimpleHUD | Pending | Compass strip / direction. |
| 26 | CoordsMod | HUD | SimpleHUD | Ported | Player position. |
| 27 | CPSDisplayMod | HUD | SimpleHUD | Ported | CPS from mouse mixin. |
| 28 | CrosshairMod | RENDER | Mod | Ported | Custom crosshair sheet. |
| 29 | CustomHeldItemsMod | RENDER | Mod | Pending | Held item model offset. |
| 30 | DamageParticlesMod | RENDER | Mod | Pending | Damage particles. |
| 31 | DamageTiltMod | PLAYER | Mod | Pending | Hurt camera tilt. |
| 32 | DamageTintMod | RENDER | Mod | Pending | Damage red overlay. |
| 33 | DayCounterMod | HUD | SimpleHUD | Pending | World day count. |
| 34 | DiscordRPCMod | OTHER | Mod | Pending | Discord IPC. |
| 35 | EarsMod | PLAYER | Mod | Pending | Cosmetic ears layer. |
| 36 | EntityCullingMod | OTHER | Mod | Pending | Entity frustum culling extras. |
| 37 | FarCameraMod | RENDER | Mod | Pending | Third-person distance. |
| 38 | FemaleGenderMod | PLAYER | Mod | Pending | Slim model override. |
| 39 | FovModifierMod | PLAYER | Mod | Pending | FOV slider. |
| 40 | FPSBoostMod | OTHER | Mod | Pending | Perf toggles; Sodium covers most. |
| 41 | FPSDisplayMod | HUD | SimpleHUD | Ported | getCurrentFps(). |
| 42 | FPSLimiterMod | OTHER | Mod | Pending | Framerate cap. |
| 43 | FPSSpooferMod | OTHER | Mod | Pending | FPS spoof display. |
| 44 | FreelookMod | PLAYER | Mod | Ported | Freelook camera. |
| 45 | FullbrightMod | PLAYER | Mod | Ported | Gamma override. |
| 46 | GlintColorMod | RENDER | Mod | Pending | Enchant glint colour. |
| 47 | InternalSettingsMod | OTHER | Mod | Ported | Theme/language/menu key via Settings. |
| 48 | GodbridgeAssistMod | HUD | HUD | Pending | Godbridge assist HUD. |
| 49 | HealthDisplayMod | HUD | SimpleHUD | Pending | Health text. |
| 50 | HitBoxMod | RENDER | Mod | Pending | Entity hitbox render. |
| 51 | HitColorMod | RENDER | Mod | Pending | Hurt flash colour. |
| 52 | HitDelayFixMod | PLAYER | Mod | Impossible | 1.8 miss-hit delay (leftClickCounter); 1.20.1 uses attack cooldown. |
| 53 | HorseStatsMod | HUD | HUD | Pending | Horse stats panel. |
| 54 | HypixelMod | OTHER | Mod | Pending | Hypixel helpers (AutoGG etc.). |
| 55 | HypixelQuickPlayMod | PLAYER | Mod | Pending | Hypixel quick play. |
| 56 | ImageDisplayMod | HUD | HUD | Pending | Image overlay. |
| 57 | InventoryDisplayMod | HUD | HUD | Pending | Inventory mirror HUD. |
| 58 | InventoryMod | OTHER | Mod | Pending | Inventory animation tweaks. |
| 59 | ItemInfoMod | RENDER | Mod | Pending | Tooltip colour info. |
| 60 | ItemPhysicsMod | RENDER | Mod | Pending | Dropped item physics. |
| 61 | Items2DMod | RENDER | Mod | Pending | Flat item rendering. |
| 62 | JumpCircleMod | RENDER | Mod | Pending | Jump ring effect. |
| 63 | KeystrokesMod | HUD | HUD | Ported | WASD/mouse keystrokes. |
| 64 | KillEffectsMod | RENDER | Mod | Pending | Kill effect particles. |
| 65 | KillSoundsMod | OTHER | Mod | Pending | Sound on kill. |
| 66 | MechvibesMod | OTHER | Mod | Pending | Keyboard sound packs. |
| 67 | MemoryUsageMod | HUD | SimpleHUD | Pending | Heap usage. |
| 68 | MinemenMod | OTHER | Mod | Pending | Minemen helpers. |
| 69 | MinimalDamageShakeMod | RENDER | Mod | Pending | Reduced hurt shake. |
| 70 | MinimalViewBobbingMod | RENDER | Mod | Pending | Reduced view bobbing. |
| 71 | MinimapMod | HUD | HUD | Pending | Chunk-atlas minimap. |
| 72 | MoBendsMod | PLAYER | Mod | Pending | Animated player model (large). |
| 73 | ModernHotbarMod | HUD | HUD | Pending | Hotbar restyle. |
| 74 | MotionBlurMod | RENDER | Mod | Pending | Motion blur post. |
| 75 | MouseStrokesMod | HUD | HUD | Pending | Mouse-stroke overlay. |
| 76 | MusicPlayerMod | HUD | HUD | Ported | Local music player. |
| 77 | NameDisplayMod | HUD | SimpleHUD | Pending | Player name text. |
| 78 | NameProtectMod | PLAYER | Mod | Pending | Hide own name. |
| 79 | NametagMod | PLAYER | Mod | Pending | Nametag restyle. |
| 80 | OverlayEditorMod | RENDER | Mod | Pending | Hide HUD overlays. |
| 81 | PackDisplayMod | HUD | HUD | Pending | Resource pack list. |
| 82 | ParticleCustomizerMod | RENDER | Mod | Pending | Particle settings. |
| 83 | PingDisplayMod | HUD | SimpleHUD | Ported | Latency text. |
| 84 | PlayerCounterMod | HUD | SimpleHUD | Pending | Online player count. |
| 85 | PlayerDisplayMod | HUD | HUD | Pending | 3D player preview. |
| 86 | PlayerListMod | HUD | HUD | Pending | Tab list changes. |
| 87 | PlayerPredicatorMod | WORLD | Mod | Pending | Player prediction helper. |
| 88 | PlayTimeDisplayMod | HUD | SimpleHUD | Pending | Session play time. |
| 89 | PotionCounterMod | HUD | SimpleHUD | Pending | Healing pot count. |
| 90 | PotionStatusMod | HUD | HUD | Pending | Active potion list. |
| 91 | ProjectileTrailMod | PLAYER | Mod | Pending | Projectile trail. |
| 92 | QuickSwitchMod | PLAYER | Mod | Pending | Sword quick switch. |
| 93 | RawInputMod | OTHER | Mod | Pending | Raw mouse input. |
| 94 | ReachCirclesMod | PLAYER | Mod | Pending | Reach visualisation circles. |
| 95 | ReachDisplayMod | HUD | SimpleHUD | Pending | Last hit reach distance. |
| 96 | RearviewMod | HUD | HUD | Pending | Rear camera (2nd pass). |
| 97 | ScoreboardMod | HUD | HUD | Pending | Scoreboard restyle. |
| 98 | ServerIPDisplayMod | HUD | SimpleHUD | Pending | Server address. |
| 99 | SessionInfoMod | HUD | HUD | Pending | Session info panel. |
| 100 | ShinyPotsMod | RENDER | Mod | Pending | Potion item shine. |
| 101 | Skin3DMod | RENDER | Mod | Pending | 3D skin layers (large). |
| 102 | SkinProtectMod | PLAYER | Mod | Pending | Replace own skin. |
| 103 | SlowSwingMod | PLAYER | Mod | Pending | Swing delay. |
| 104 | SoundModifierMod | OTHER | Mod | Pending | Per-category volume/pitch. |
| 105 | SoundSubtitlesMod | HUD | HUD | Pending | Sound subtitles HUD. |
| 106 | SpeedometerMod | HUD | SimpleHUD | Pending | Speed text/graph. |
| 107 | StopwatchMod | HUD | SimpleHUD | Pending | Stopwatch. |
| 108 | TabEditorMod | RENDER | Mod | Pending | Tab list background. |
| 109 | TaplookMod | PLAYER | Mod | Pending | Tap-to-look (Freelook variant). |
| 110 | TargetIndicatorMod | RENDER | Mod | Pending | Target indicator. |
| 111 | TargetInfoMod | HUD | HUD | Pending | Target info panel. |
| 112 | TimeChangerMod | WORLD | Mod | Pending | Client-side time. |
| 113 | TNTTimerMod | HUD | SimpleHUD | Pending | TNT fuse timer. |
| 114 | ToggleSneakMod | PLAYER | Mod | Pending | Toggle sneak. |
| 115 | ToggleSprintMod | HUD | SimpleHUD | Ported | Toggle sprint + HUD. |
| 116 | UHCOverlayMod | RENDER | Mod | Pending | UHC overlay. |
| 117 | WaveyCapesMod | RENDER | Mod | Pending | Cape physics. |
| 118 | WaypointMod | WORLD | Mod | Pending | Waypoints. |
| 119 | WeatherChangerMod | WORLD | Mod | Pending | Client-side weather. |
| 120 | WeatherDisplayMod | HUD | SimpleHUD | Pending | Weather text. |
| 121 | ZoomMod | PLAYER | Mod | Ported | Smooth zoom. |

Counts: Ported=14, Pending=105, Impossible=2. Total 121.

## 1.20.1-only (keep)

| Class | Category | Type | Status | Notes |
|---|---|---|---|---|
| TotemCounterModule | HUD | HUD | Modern-only | 1.20.1 totem pops counter (not in 1.8.9). |
| ElytraStatusModule | HUD | HUD | Modern-only | 1.20.1 elytra durability HUD (not in 1.8.9). |
| CapeModule | PLAYER | Mod | Modern-only | Cape cosmetics module (1.8.9 uses CapeManager, not a Mod). |

## Batch plan

1. SimpleHUD text mods (Reach, Health, Day, Memory, Combo, Name, PlayTime, Players, PotionCounter, ServerIP, Speedometer, Stopwatch, Weather, Compass).
2. HUD panels (PotionStatus, BlockInfo, Calendar, SessionInfo, ArrayList, HorseStats, BossHealth, PackDisplay, MouseStrokes).
3. Render basics (HitBox, ChunkBorders, BlockOverlay, Fullbright-siblings, Damage*, Minimal*, OverlayEditor, HitColor).
4. Player/QoL (ToggleSneak, Taplook, BowZoom, FovModifier, AppleSkin, NameProtect, SlowSwing, QuickSwitch, RawInput, Borderless).
5. Remaining RENDER/WORLD/OTHER (large subsystems last: Minimap, Skin3D, MoBends, WaveyCapes, Rearview, MotionBlur).

