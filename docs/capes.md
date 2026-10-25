# Custom capes in Confu Client

## Use
1. Open the Confu Client menu (Options → *Open Confu Client menu*, or Right Shift) → **Cosmetics** → **Custom**.
2. Click **+** and pick a PNG, **or** drop PNGs into the folder (folder button, which opens
   `.minecraft/glide/cache/custom-cape/`) and click **reload** (circular arrow).
3. Click a cape card to equip it. A card's **trash** button (shown on hover) deletes that cape.
4. Press F5 to see it in third person. The choice is saved in your profile
   (`glide/profile/Default.json`, key `text.customcape`) when the menu closes, and is
   restored on the next start.

## Accepted images
PNG only. The file signature is checked, so a JPEG renamed to `.png` is refused. Max 8 MB.

| Kind | Sizes | What it is |
|---|---|---|
| Cape texture | 64x32, 128x64, 256x128 … 2048x1024 | the vanilla cape layout (front face at u1–11, v1–17) |
| Cape art | 22x17, 44x34, 88x68 … | just the cape area, as cape editors export it; placed at the top-left of a 64x32 canvas |

Rejected: any other size (for example 64x64 skins), files larger than 2048x1024, and
images whose front face is fully transparent. Imports are written as normalised 64:32
PNGs, so the stored file is always a standard cape texture. Files that fail
validation in the folder are skipped (and logged); they are never deleted.

Previews come from the cape's front face, scaled up with nearest-neighbour so the
pixels stay sharp. They are cached in `glide/cache/cape/` and keyed by file name plus
content hash, so replacing a file never shows a stale preview.

## Who sees it
**Only you.** Capes are local: the client swaps the texture returned for *your own*
player (`AbstractClientPlayer#getLocationCape`). Upstream Glide/Soar has no cape sync
server in this codebase. No cape code makes network calls, and the only remote URLs
are news, changelog, update and blacklist JSON on glideclient.github.io. Other players
see your Mojang cape (or none). Showing it to others would need a shared cosmetics
server, which this fork does not have.

Elytra don't exist in 1.8.9. In first person the cape isn't visible, as in vanilla. It
shows in F5 views and in the inventory preview. The vanilla *Skin Customization → Cape*
toggle still hides it.

## Upstream `cosmetics` branch
That branch is an unfinished rewrite. It adds a cosmetics framework
(`eu.shoroa.contrib`: `CosmeticManager`, `CosmeticLayer`), a halo and a "female gender"
model cosmetic, and a fake-world 3D preview (`FakeWorld`, `FakePlayer`, about 1000 lines).
It also moves the cape grid into a `CapesScene`. It has no cape networking either.
Nothing was ported: the cape grid is what we already have, and the 3D preview is a
large, unfinished subsystem.
