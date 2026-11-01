# MOD_PARITY — 1.8.9 → 1.20.1 Fabric

Source: `src/.../mods/ModManager.java` (121 mods). Target: `modern/` (Fabric 1.20.1).
Branch: `modern-allmods`. 1.8.9 tree (`src/`) is not modified.

## Status legend

| Status | Meaning |
|---|---|
| Ported | Present in `modern/` ModuleManager and builds. |
| Pending | Must be ported for parity. |
| Impossible | Cannot match 1.8.9 on 1.20.1; reason in Notes. |
| Skeleton | Registered in menu with settings; full 1.8.9 visuals/hooks still incomplete. |
| Modern-only | Exists only on 1.20.1; keep. |

## Modules (121 from 1.8.9)

| # | 1.8.9 class | Category | Type | Status | Notes |
|---|---|---|---|---|---|
| 1 | AnimationsMod | RENDER | Mod | Ported | All 10 settings; block-hit/pushing via isBreakingBlock mixin, smooth sneak via Camera.updateEyeHeight. |
| 2 | AppleSkinMod | PLAYER | Mod | Ported | Hunger/saturation overlay on food bar. |
| 3 | ArmorStatusMod | HUD | HUD | Ported | Worn armour icons, H/V layout. |
| 4 | ArrayListMod | HUD | HUD | Ported | Enabled-module list. |
| 5 | AsyncScreenshotMod | OTHER | Mod | Ported | Off-thread PNG write. |
| 6 | AutoTextMod | PLAYER | Mod | Ported | Keybind sends preset chat. |
| 7 | BlockInfoMod | HUD | HUD | Ported | Looked-at block name. |
| 8 | BlockOverlayMod | RENDER | Mod | Ported | Block outline restyle. |
| 9 | BloodParticlesMod | RENDER | Mod | Ported | Redstone-crack burst + stone sound on hovered target hit. |
| 10 | BorderlessFullscreenMod | OTHER | Mod | Ported | GLFW borderless fullscreen. |
| 11 | BossHealthMod | HUD | HUD | Ported | Boss bar position/scale. |
| 12 | BowZoomMod | PLAYER | Mod | Ported | Bow FOV; reuses Zoom FOV hook. |
| 13 | BreadcrumbsMod | RENDER | Mod | Ported | Path trail. |
| 14 | CalendarMod | HUD | HUD | Ported | Month calendar widget. |
| 15 | ChatMod | OTHER | Mod | Ported | ChatHud mixin: smooth fade/offset, head icons, infinity, background, compact duplicates. |
| 16 | ChatTranslateMod | OTHER | Mod | Ported | Microsoft endpoint; green pencil icon, ChatScreen On/Off + To boxes, translated outgoing chat. |
| 17 | ChunkAnimatorMod | RENDER | Mod | Impossible | Needs chunk-rebuild hooks; conflicts with Sodium (project rule). |
| 18 | ChunkBordersMod | RENDER | Mod | Ported | World-render chunk borders. |
| 19 | ClearGlassMod | RENDER | Mod | Ported | TransparentBlock.isSideInvisible mixin; Normal/Stained toggles, chunk rebuild on change. |
| 20 | ClearWaterMod | RENDER | Mod | Ported | Water fog/texture. |
| 21 | ClientSpooferMod | OTHER | Mod | Ported | Client brand spoof. |
| 22 | ClockMod | HUD | SimpleHUD | Ported | Clock text. |
| 23 | ColorSaturationMod | RENDER | Mod | Skeleton | module registered; post shader TBD |
| 24 | ComboCounterMod | HUD | SimpleHUD | Ported | Hit combo counter. |
| 25 | CompassMod | HUD | SimpleHUD | Ported | Compass strip / direction. |
| 26 | CoordsMod | HUD | SimpleHUD | Ported | Player position. |
| 27 | CPSDisplayMod | HUD | SimpleHUD | Ported | CPS from mouse mixin. |
| 28 | CrosshairMod | RENDER | Mod | Ported | Custom crosshair sheet. |
| 29 | CustomHeldItemsMod | RENDER | Mod | Ported | First-person X/Y/Z + X/Y/Z Scale at HeldItemRenderer.renderItem HEAD. |
| 30 | DamageParticlesMod | RENDER | Mod | Ported | Floating damage/heal numbers via world-render text. |
| 31 | DamageTiltMod | PLAYER | Mod | Ported | Hurt camera tilt. |
| 32 | DamageTintMod | RENDER | Mod | Ported | Damage red overlay. |
| 33 | DayCounterMod | HUD | SimpleHUD | Ported | World day count. |
| 34 | DiscordRPCMod | OTHER | Mod | Ported | Local Discord IPC; application ID set in the Soar menu text box. |
| 35 | EarsMod | PLAYER | Mod | Skeleton | module registered; player model layer TBD |
| 36 | EntityCullingMod | OTHER | Mod | Ported | GL occlusion queries in EntityRenderDispatcher.render; label kept, marker/invisible handling, distance limit. |
| 37 | FarCameraMod | RENDER | Mod | Ported | Third-person distance. |
| 38 | FemaleGenderMod | PLAYER | Mod | Skeleton | module registered; slim model TBD |
| 39 | FovModifierMod | PLAYER | Mod | Ported | FOV slider. |
| 40 | FPSBoostMod | OTHER | Mod | Ported | Perf toggles; Sodium covers most. |
| 41 | FPSDisplayMod | HUD | SimpleHUD | Ported | getCurrentFps(). |
| 42 | FPSLimiterMod | OTHER | Mod | Ported | Framerate cap. |
| 43 | FPSSpooferMod | OTHER | Mod | Ported | FPS spoof display. |
| 44 | FreelookMod | PLAYER | Mod | Ported | Freelook camera. |
| 45 | FullbrightMod | PLAYER | Mod | Ported | Gamma override. |
| 46 | GlintColorMod | RENDER | Mod | Ported | Enchant glint colour. |
| 47 | InternalSettingsMod | OTHER | Mod | Ported | Theme/language/menu key via Settings. |
| 48 | GodbridgeAssistMod | HUD | HUD | Ported | Godbridge assist HUD. |
| 49 | HealthDisplayMod | HUD | SimpleHUD | Ported | Health text. |
| 50 | HitBoxMod | RENDER | Mod | Ported | Entity hitbox render. |
| 51 | HitColorMod | RENDER | Mod | Ported | Hurt flash colour. |
| 52 | HitDelayFixMod | PLAYER | Mod | Impossible | 1.8 miss-hit delay (leftClickCounter); 1.20.1 uses attack cooldown. |
| 53 | HorseStatsMod | HUD | HUD | Ported | Horse stats panel. |
| 54 | HypixelMod | OTHER | Mod | Ported | Hypixel helpers (AutoGG etc.). |
| 55 | HypixelQuickPlayMod | PLAYER | Mod | Ported | Hypixel quick play. |
| 56 | ImageDisplayMod | HUD | HUD | Ported | Image overlay. |
| 57 | InventoryDisplayMod | HUD | HUD | Ported | Inventory mirror HUD. |
| 58 | InventoryMod | OTHER | Mod | Ported | Normal/EaseBackIn open animation, background toggle (all container screens), particle field; 1.20.1 never shifts on potion effects so prevent-potion-shift is inherent. |
| 59 | ItemInfoMod | RENDER | Mod | Ported | Held-item name/enchants/potion info above hotbar, 1.8 colours. |
| 60 | ItemPhysicsMod | RENDER | Mod | Ported | ItemEntityRenderer: press-to-ground Y, -90° X for non-blocks, airborne (1,1,1) spin; disables Items2D. |
| 61 | Items2DMod | RENDER | Mod | Ported | Flat (non-3D) dropped items billboard to camera; disables ItemPhysics with notification. |
| 62 | JumpCircleMod | RENDER | Mod | Ported | Jump ring effect. |
| 63 | KeystrokesMod | HUD | HUD | Ported | WASD/mouse keystrokes. |
| 64 | KillEffectsMod | RENDER | Mod | Ported | Lightning/flame/cloud/blood effects + sounds on target removal. |
| 65 | KillSoundsMod | OTHER | Mod | Ported | Sound on kill. |
| 66 | MechvibesMod | OTHER | Mod | Skeleton | module registered; key sound packs TBD |
| 67 | MemoryUsageMod | HUD | SimpleHUD | Ported | Heap usage. |
| 68 | MinemenMod | OTHER | Mod | Ported | Auto-play: joins /requeue from chat click events. |
| 69 | MinimalDamageShakeMod | RENDER | Mod | Ported | Reduced hurt shake. |
| 70 | MinimalViewBobbingMod | RENDER | Mod | Ported | Reduced view bobbing. |
| 71 | MinimapMod | HUD | HUD | Skeleton | module registered; chunk atlas TBD |
| 72 | MoBendsMod | PLAYER | Mod | Skeleton | module registered; large animation subsystem TBD |
| 73 | ModernHotbarMod | HUD | HUD | Ported | 4 designs, smooth anim, pickup style; vanilla hotbar/XP hidden via InGameHudMixin. |
| 74 | MotionBlurMod | RENDER | Mod | Skeleton | module registered; post-process TBD |
| 75 | MouseStrokesMod | HUD | HUD | Ported | Mouse-stroke overlay. |
| 76 | MusicPlayerMod | HUD | HUD | Ported | Local music player. |
| 77 | NameDisplayMod | HUD | SimpleHUD | Ported | Player name text. |
| 78 | NameProtectMod | PLAYER | Mod | Ported | Hide own name. |
| 79 | NametagMod | PLAYER | Mod | Ported | Nametag restyle. |
| 80 | OverlayEditorMod | RENDER | Mod | Ported | Hide HUD overlays. |
| 81 | PackDisplayMod | HUD | HUD | Ported | Resource pack list. |
| 82 | ParticleCustomizerMod | RENDER | Mod | Ported | Extra crit/enchanted-hit particles, 1.8 multipliers. |
| 83 | PingDisplayMod | HUD | SimpleHUD | Ported | Latency text. |
| 84 | PlayerCounterMod | HUD | SimpleHUD | Ported | Online player count. |
| 85 | PlayerDisplayMod | HUD | HUD | Ported | Local player paperdoll via entity render dispatcher. |
| 86 | PlayerListMod | HUD | HUD | Ported | Player heads + names, max setting, auto height. |
| 87 | PlayerPredicatorMod | WORLD | Mod | Skeleton | module registered; prediction render TBD |
| 88 | PlayTimeDisplayMod | HUD | SimpleHUD | Ported | Session play time. |
| 89 | PotionCounterMod | HUD | SimpleHUD | Ported | Healing pot count. |
| 90 | PotionStatusMod | HUD | HUD | Ported | Active potion list. |
| 91 | ProjectileTrailMod | PLAYER | Mod | Skeleton | module registered; trail FX TBD |
| 92 | QuickSwitchMod | PLAYER | Mod | Ported | Sword quick switch. |
| 93 | RawInputMod | OTHER | Mod | Ported | Raw mouse input. |
| 94 | ReachCirclesMod | PLAYER | Mod | Ported | Reach visualisation circles. |
| 95 | ReachDisplayMod | HUD | SimpleHUD | Ported | Last hit reach distance. |
| 96 | RearviewMod | HUD | HUD | Skeleton | module registered; 2nd camera TBD |
| 97 | ScoreboardMod | HUD | HUD | Ported | Scoreboard restyle. |
| 98 | ServerIPDisplayMod | HUD | SimpleHUD | Ported | Server address. |
| 99 | SessionInfoMod | HUD | HUD | Ported | Session info panel. |
| 100 | ShinyPotsMod | RENDER | Mod | Ported | Glint layer override via RenderLayer mixin (no equal-depth mask). |
| 101 | Skin3DMod | RENDER | Mod | Skeleton | module registered; 3D skin layers TBD |
| 102 | SkinProtectMod | PLAYER | Mod | Ported | Replace own skin. |
| 103 | SlowSwingMod | PLAYER | Mod | Ported | Swing delay. |
| 104 | SoundModifierMod | OTHER | Mod | Ported | Per-category volume/pitch. |
| 105 | SoundSubtitlesMod | HUD | HUD | Ported | Sound subtitles HUD. |
| 106 | SpeedometerMod | HUD | SimpleHUD | Ported | Speed text/graph. |
| 107 | StopwatchMod | HUD | SimpleHUD | Ported | Stopwatch. |
| 108 | TabEditorMod | RENDER | Mod | Ported | Tab list background. |
| 109 | TaplookMod | PLAYER | Mod | Ported | Tap-to-look (Freelook variant). |
| 110 | TargetIndicatorMod | RENDER | Mod | Ported | Animated target ring; colour presets instead of RGB picker. |
| 111 | TargetInfoMod | HUD | HUD | Ported | Target info panel. |
| 112 | TimeChangerMod | WORLD | Mod | Ported | Client-side time. |
| 113 | TNTTimerMod | HUD | SimpleHUD | Ported | TNT fuse timer. |
| 114 | ToggleSneakMod | PLAYER | Mod | Ported | Toggle sneak. |
| 115 | ToggleSprintMod | HUD | SimpleHUD | Ported | Toggle sprint + HUD. |
| 116 | UHCOverlayMod | RENDER | Mod | Ported | UHC overlay. |
| 117 | WaveyCapesMod | RENDER | Mod | Skeleton | module registered; StickSimulation TBD |
| 118 | WaypointMod | WORLD | Mod | Ported | Waypoints. |
| 119 | WeatherChangerMod | WORLD | Mod | Ported | Client-side weather. |
| 120 | WeatherDisplayMod | HUD | SimpleHUD | Ported | Weather text. |
| 121 | ZoomMod | PLAYER | Mod | Ported | Smooth zoom. |

Counts: Ported=107, Pending=0, Skeleton=12, Impossible=2. Total 121.

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

