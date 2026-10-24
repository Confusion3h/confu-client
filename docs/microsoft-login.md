# Microsoft account login

The launcher can start Minecraft with a Microsoft account (`--userType msa`) or with the offline name. Offline is the default. The refresh token is stored only on this computer, in `%APPDATA%\.CONFUSION3H\msa.properties` on Windows and `~/.CONFUSION3H/msa.properties` elsewhere. It is not part of the git repo. Delete that file (or press **Sign out**) to disconnect. The short-lived Minecraft access token is not written to disk.

No client secret is used or accepted. Sign-in opens Microsoft's page in the system browser. A device code is the fallback.

## 1. Register an Azure application

1. Open [Microsoft Entra admin center](https://entra.microsoft.com/) → **App registrations** → **New registration**.
2. Name it whatever you like (for example `CONFUSION3H`).
3. Supported account types: **Personal Microsoft accounts only** (or personal + work, if you prefer).
4. Redirect URI: leave empty. If the portal insists, choose **Public client / native** and `https://login.microsoftonline.com/common/oauth2/nativeclient`. Device code does not use it.
5. Register, then copy the **Application (client) ID** (a UUID).
6. **Authentication** → **Advanced settings** → **Allow public client flows** = **Yes**. Save.
7. Do not create a client secret. If you create one, do not put it in the launcher or the repo.
8. **Authentication** → **Redirect URIs** → add exactly `http://127.0.0.1:63212/callback` (public client / mobile and desktop). Save.

Paste the client ID into the launcher: **Settings** → **Microsoft client ID**. That value is saved in `launcher.properties`. It identifies your app; it is not a password.

## 2. Sign in

On the home page choose **Microsoft**, then **Sign in**. The launcher opens the system browser on Microsoft's own sign-in page (authorization code + PKCE, no secret). When the page redirects to `http://127.0.0.1:63212/callback`, the launcher finishes and the browser tab says you can close it.

**Use a code** on the home page switches to the device-code fallback (`https://www.microsoft.com/link` plus a short code) if the browser flow is unavailable. Approve it with the Microsoft account that owns Minecraft Java. The launcher then exchanges that login for Xbox Live, XSTS, a Minecraft token and the profile (name + UUID).

Press **Play**. The launcher refreshes the Microsoft token, repeats the Xbox → Minecraft exchange, and starts the game with:

- `--username` the profile name
- `--uuid` the profile id (no dashes)
- `--accessToken` the Minecraft token (redacted in the console log)
- `--userType msa`

**Offline** still uses the typed name, an offline UUID and `--userType legacy`.

## What this environment did not verify

There is no Azure application and no Microsoft account in this build environment, so the live chain was not executed against Microsoft, Xbox Live or Minecraft services. The requests follow the documented device-code flow:

1. Browser: `GET .../authorize` with PKCE (`S256`) and redirect `http://127.0.0.1:63212/callback`, then `POST .../token` with `grant_type=authorization_code`
2. Fallback: `POST .../devicecode` with scope `XboxLive.signin offline_access`, then poll `.../token` with the device-code grant
3. Refresh with `grant_type=refresh_token` on later launches
4. `POST https://user.auth.xboxlive.com/user/authenticate` (`RpsTicket` prefixed with `d=`)
5. `POST https://xsts.auth.xboxlive.com/xsts/authorize` relying party `rp://api.minecraftservices.com/`
6. `POST https://api.minecraftservices.com/authentication/login_with_xbox`
7. `GET https://api.minecraftservices.com/minecraft/profile`

Xbox Live often rejects a client ID that Microsoft has not allowed for Minecraft (the XSTS body then carries an `XErr`). A 404 on the profile means that account has no Java Edition profile. Those failures are shown in the launcher; they are not treated as success.
