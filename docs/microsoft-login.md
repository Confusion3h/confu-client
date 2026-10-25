# Microsoft account login

The launcher starts Minecraft with a Microsoft account (`--userType msa`) or with the offline name. Offline is the default. Sign-in needs no Azure application, no client secret and no setting. Press **Microsoft**, then **Sign in with Microsoft**.

On Windows and Linux a small window (Microsoft Edge, or Chrome if Edge is not installed) opens on Microsoft's own page. Type the email and password there. The launcher reads the result, closes the window, and exchanges the login for Xbox Live, XSTS, a Minecraft token and the profile name. On macOS the launcher shows a short code and opens `https://www.microsoft.com/link` in the default browser (Safari, unless you chose another). Type the code there, then sign in with the Microsoft account. The launcher keeps polling until Microsoft confirms the code, then does the same exchange. The refresh token is stored only on this computer, in `%APPDATA%\.confu\msa.properties` on Windows and `~/.confu/msa.properties` elsewhere. An existing `%APPDATA%\.CONFUSION3H` (or `~/.CONFUSION3H`) is renamed to `.confu` on startup when `.confu` is not already there. It is not part of the git repo. Delete that file, or press **Sign out**, to disconnect. The short-lived Minecraft access token is not written to disk.

## What is built in

The launcher uses the public Minecraft Live client id `00000000402b5328` (the same id other third-party launchers use). It is not a secret.

| Step | Request |
| --- | --- |
| Authorize | `GET https://login.live.com/oauth20_authorize.srf` with `response_type=code`, scope `service::user.auth.xboxlive.com::MBI_SSL`, redirect `https://login.live.com/oauth20_desktop.srf` |
| Token | `POST https://login.live.com/oauth20_token.srf` (`authorization_code`, same redirect and scope) |
| Device code (macOS) | `POST https://login.live.com/oauth20_connect.srf` with `client_id`, scope `service::user.auth.xboxlive.com::MBI_SSL` and `response_type=device_code`. Returns `user_code`, `device_code`, `verification_uri` (`https://www.microsoft.com/link`), `interval` and `expires_in` |
| Device poll (macOS) | `POST https://login.live.com/oauth20_token.srf` with `grant_type=urn:ietf:params:oauth:grant-type:device_code` and the `device_code`, every `interval` seconds until `access_token` and `refresh_token` come back. `authorization_pending` means keep waiting; `slow_down` adds 5 seconds |
| Refresh | same token URL, `grant_type=refresh_token`, on later launches |
| Xbox Live | `POST https://user.auth.xboxlive.com/user/authenticate`. `RpsTicket` is the Live access token with no `d=` prefix (that prefix is only for Azure v2 tokens) |
| XSTS | `POST https://xsts.auth.xboxlive.com/xsts/authorize`, `SandboxId` `RETAIL`, relying party `rp://api.minecraftservices.com/` |
| Minecraft | `POST https://api.minecraftservices.com/authentication/login_with_xbox` with `XBL3.0 x=<uhs>;<xsts>` |
| Profile | `GET https://api.minecraftservices.com/minecraft/profile` |

On Windows and Linux the sign-in window is Edge or Chrome in app mode (Brave, Chromium and Vivaldi count too), with its own temporary profile and a localhost debugging port. The launcher watches that window until the address is `https://login.live.com/oauth20_desktop.srf?code=…`, then closes it. Nothing is pasted, and nothing is typed into Settings.

If none of those browsers is installed, Sign in opens the same page in the default browser and still reads the code itself. On Windows it is the default browser, then the address bar. On Linux, Firefox is started with its debugging port when it is the browser that remains; a Chromium desktop file from `xdg-settings` is used the same way as Chrome.

On macOS the launcher does not read any browser. It never looks for Chrome, Edge, Opera or another app, and it never asks macOS for Automation access to a browser. That is why the device code is used there: the browser that opens is whichever one macOS uses for web pages, Safari included, and the login completes on Microsoft's page. The client id and the redirect stay the ones in the table. A declined login, a closed window, a timeout, a missing Xbox profile, a child account, or an account with no Minecraft Java profile is shown as a sentence under the Play button. Those failures do not open Settings.

Press **Play**. The launcher refreshes the Microsoft token, repeats the Xbox → Minecraft exchange, and starts the game with:

- `--username` the profile name
- `--uuid` the profile id (no dashes)
- `--accessToken` the Minecraft token (redacted in the console log)
- `--userType msa`

**Offline** still uses the typed name, an offline UUID and `--userType legacy`.

## Advanced override

Settings does not ask for a client id. `msClientId` in `launcher.properties` is optional. Leave it empty. A 16-hex Live client id replaces the built-in one and still uses the sign-in flow for your platform described above. An Azure application UUID (the value an older build asked you to paste) is ignored, so a previous settings file cannot send Sign in back to Settings.

## What this environment did not verify

No Microsoft account is available here, so a real password login was not completed, and the macOS device code was not entered on a real Mac. The checks that did run:

- The authorize URL, token form, Xbox JSON, XSTS JSON and Minecraft `identityToken` match the table above, including no `d=` on `RpsTicket`.
- A sample debugging-port page list yields the `code` only from `oauth20_desktop.srf`, not from the authorize URL that merely mentions that redirect.
- `GET` of the authorize URL returns Microsoft's sign-in page for client `00000000402b5328` (not `invalid_client`).
- `POST` of a fake code to the token URL is rejected as a bad code, not as a bad client.
- `POST` of a fake ticket to Xbox Live is rejected by Xbox, and the JSON body is accepted.
- Opening Chrome on `https://example.com/` and reading the page address back from the debugging port succeeds on this machine. The same window code is what Sign in uses for `login.live.com` on Windows and Linux.
- `POST` of the device-code request to `oauth20_connect.srf` returns a `user_code`, a `device_code`, `verification_uri` `https://www.microsoft.com/link`, `interval` 5 and `expires_in` 900. Polling `oauth20_token.srf` with the device grant returns `authorization_pending` until the code is entered. Run `java -cp <classes> confusione.launcher.core.MicrosoftAuth --online` to repeat this.
- The device-code path has no browser or Automation step, so nothing on macOS depends on which browser is installed.
