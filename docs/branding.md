# CONFUSION3H branding

Original mark for **CONFUSION3H** (also written CONFUSION3H). It sits in the same
family as the Soar/Glide marks (outer ring + round-capped monoline letter, flat
white in-game) but the letterform is new: a C, not a G, with a curled terminal and
a centre dot instead of a crossbar.

## Files
| File | Use |
|---|---|
| `branding/CONFUSION3H-mark.svg` | primary mark, gradient, transparent |
| `branding/CONFUSION3H-mark-white.svg` | monochrome white (in-game, tinted by NanoVG) |
| `branding/CONFUSION3H-app-icon.svg` | app icon, dark rounded square, >= 64 px |
| `branding/CONFUSION3H-app-icon-small.svg` | app icon <= 32 px: no ring, heavier C |
| `branding/gen.py` | writes the SVGs above and renders `branding/png/` (needs `cairosvg`) |
| `branding/png/icon_{16,32,64,128,256}.png` | window/app icons |
| `branding/png/mark-{256,512}.png`, `mark-white-512.png`, `osx-1024.png` | logos |

Copied into the builds:
- client: `src/main/resources/assets/minecraft/soar/logo/` (`mark-white.png`, `icon_*.png`, SVG) and `soar/osx.png` (dock icon, 256 px)
- launcher: `launcher/src/main/resources/confusione/icons/icon_*.png`; the animated logo is drawn in code from the same geometry (`launcher/.../ui/Mark.java`)

## Geometry (256 x 256 box, centre 128,128, SVG y-down angles)
- **Ring**: circle r = 110, stroke 16.
- **C**: arc r = 62, stroke 20, round caps/joins, from +40 deg (lower right) the long way
  round (280 deg) to -40 deg (upper right). Path:
  `M 175.49 167.85 A 62 62 0 1 1 175.49 88.15`
- **Curl**: continues from the upper terminal with `A 30 30 0 0 0 141.83 96.94`, a
  small inward hook (r30 circle centred at 164.84,116.19) that reads as a swirl.
- **Dot**: circle r = 11 at the centre.
- **Small variant** (<= 32 px): no ring, C r = 84 stroke 34, curl r = 40, dot r = 17.
- **App icon** background: rounded square, radius 56, `#0B0A12`.
- Clear space: 1/8 of the mark width on every side. Minimum size: 16 px (small variant).

## Colors
| Name | Hex | Use |
|---|---|---|
| Violet | `#7C5CFF` | gradient start (top-left) |
| Pink | `#FF4FA3` | gradient middle (55 %) |
| Amber | `#FFB547` | gradient end (bottom-right), centre dot |
| Ink | `#0B0A12` | icon / launcher background |
| Text | `#F2F0FA` | light text |
| Muted | `#9A96B0` | secondary text |

Gradient: linear, (28,28) to (228,228) in the 256 box.

These colours are the **client** palette (in-game assets, README art). 

### Launcher palette (monochrome only)
The launcher uses only black, greys and white: backdrop glows, the wordmark
gradient, buttons, highlights, its logo and its window icons.

| Name | Hex | Use |
|---|---|---|
| White | `#FFFFFF` | gradient start, centre dot, OK status |
| Light grey | `#C8C8C8` | gradient middle (55 %) |
| Mid grey | `#8C8C8C` | gradient end |
| Ink | `#0A0A0A` | background, launcher icon square |
| On-accent | `#111111` | text and knobs drawn on the light gradient |
| Text | `#F5F5F5` | light text |
| Muted | `#9A9A9A` | secondary text |
| Error | `#D6D6D6` | error status (told apart by wording, not hue) |

Files: `branding/CONFUSION3H-{mark,app-icon,app-icon-small}-mono.svg`,
`branding/png/mark-mono-512.png`; `gen.py` also writes the launcher's
`icon_*.png`. In code: `launcher/.../ui/Theme.java`.

## Type
Inter (SIL OFL 1.1), Regular and SemiBold, uppercase wordmark "CONFUSION3H" (always written exactly so in user-facing text).

## Attribution
CONFUSION3H is a GPL-3.0 fork of Glide Client, itself based on Soar Client by
EldoDebug. The mark is new artwork; upstream names stay in the credits and README.
