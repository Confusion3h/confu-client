# Music in CONFUSION3H

The **Music Player** HUD mod (mod menu, off by default) has two sources:

| Source | What plays the audio | Needs |
|---|---|---|
| Local files | CONFUSION3H itself | nothing to install |
| Spotify | your Spotify app (desktop, phone, web player); CONFUSION3H is a remote control | a Spotify **Premium** account and your own Spotify app Client ID |

## Install: nothing

All decoding is pure Java and ships inside the client jar. **FFmpeg is not
needed**, and neither are JavaFX, VLC/vlcj or OpenAL.

| Format | Decoder | Where it comes from |
|---|---|---|
| `.mp3` | JLayer 1.0.1 (`javazoom:jlayer`, LGPL-2.1) | embedded in the client jar |
| `.ogg` (Vorbis) | JOrbis | already part of Minecraft 1.8.9 (`codecjorbis-20101023.jar`) |
| `.wav`, `.aiff`, `.au` | `javax.sound.sampled` | the JRE |

Not supported: FLAC, AAC/M4A, Opus, WMA. Convert them, for example
`ffmpeg -i in.flac out.ogg` (only if you want to; not needed to run the client).

Output goes to the system default device through `javax.sound` (on Linux that is
ALSA/PulseAudio/PipeWire, which every desktop already has). If there is no audio
device, the mod shows "No audio output available" and stops.

## Local files

1. Put files in `.minecraft/glide/music/` (created on first launch;
   `.soarcmd music folder` opens it).
2. Enable **Music Player**, Source = **Local files**.
3. Use the keybinds in the mod settings, or chat commands (below).

Name files `Artist - Title.mp3` to get the artist line in the HUD. Volume and shuffle
are mod settings; playback stops when the mod is turned off.

## Spotify

No client secret is used anywhere. CONFUSION3H uses the Authorization Code flow
with **PKCE**, so only a public Client ID is needed, and you supply your own.

1. Go to <https://developer.spotify.com/dashboard>, log in, **Create app**.
2. Under **Redirect URIs** add exactly `http://127.0.0.1:8888/callback`.
   (Spotify rejects `localhost`; use the `127.0.0.1` literal.)
3. Under **APIs used** tick **Web API**. Save.
4. Copy the app's **Client ID**. Ignore the Client secret.
5. In game: Music Player settings, set Source = **Spotify** and paste the ID in
   **Spotify Client ID**.
6. In chat: `.soarcmd music connect`. A browser opens on Spotify's consent page.
   Accept it and the page says "Connected".
7. Start playing something in any Spotify app. The HUD shows it within ~3 s.

While the app is in Spotify's *Development mode*, only accounts you add under
**User Management** in the dashboard can connect (up to 25).

Scopes requested: `user-read-playback-state user-modify-playback-state
user-read-currently-playing`.

The refresh token (no secret, no password) is stored in
`.minecraft/glide/music/spotify.properties`. Delete that file or run
`.soarcmd music disconnect` to revoke it locally. To revoke it fully, remove the app
at <https://www.spotify.com/account/apps/>.

Playback control (play, pause, skip, volume) is a Spotify **Premium** feature. Free
accounts can still see what is playing, but control commands return
"Premium is required". "No active Spotify device" means no Spotify app is
currently playing or paused.

## Commands

`.soarcmd music <sub>`: `status` (default), `connect`, `disconnect`, `play`,
`pause`, `toggle`, `next`, `prev`, `vol 0-100`, `reload` (rescan the folder),
`folder`.

## Cost when idle (FPS mode)

- Mod off: not on the event bus. No thread, no timer, no socket.
- Mod on, nothing playing: one cheap settings check per tick. The HUD draws nothing.
- Local playing: one low-priority daemon thread decodes ~4 KB at a time into a
  250 ms buffer, and waits on a lock while paused. The HUD gets a new snapshot once
  per second and extrapolates the progress bar between snapshots.
- Spotify: one HTTPS `GET /me/player` every 3 s on a background thread, backing off
  for 15 s after errors. Never on the render thread.

## Compared with upstream `spotify-player`

Glide's `origin/spotify-player` branch was a Spotify-only remote control built on
`spotify-web-api-java` 6.5.4 (httpclient5) and Gson 2.8.6. It used a client secret
kept in a properties file, and it did not bundle Gson, which clashes with
Minecraft's Gson 2.2.4. CONFUSION3H keeps the idea (Web API, loopback redirect) but
uses JDK HTTP plus Minecraft's Gson, PKCE instead of a secret, and adds local playback.
