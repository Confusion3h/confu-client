package confusione.launcher.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Microsoft login for Minecraft Java, with nothing for the user to register.
 * Uses the public Minecraft Live client ({@code 00000000402b5328}) and the
 * desktop redirect {@code https://login.live.com/oauth20_desktop.srf}. A small
 * Edge or Chrome window loads Microsoft's own page. When neither is installed,
 * the default browser opens the same page (Safari included) and the code is
 * read from that tab. The code is exchanged for Xbox Live, XSTS and a Minecraft token.
 * There is no client secret. A 16-hex {@code msClientId} in launcher.properties
 * is an optional override; an Azure UUID left over from an older build is ignored.
 */
public final class MicrosoftAuth {

	/** Official Minecraft launcher public client. Not a secret. */
	public static final String BUILTIN_CLIENT_ID = "00000000402b5328";
	static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
	static final String REDIRECT = "https://login.live.com/oauth20_desktop.srf";
	private static final String AUTHORIZE = "https://login.live.com/oauth20_authorize.srf";
	private static final String TOKEN = "https://login.live.com/oauth20_token.srf";
	private static final String XBL = "https://user.auth.xboxlive.com/user/authenticate";
	private static final String XSTS = "https://xsts.auth.xboxlive.com/xsts/authorize";
	private static final String MC_LOGIN = "https://api.minecraftservices.com/authentication/login_with_xbox";
	private static final String MC_PROFILE = "https://api.minecraftservices.com/minecraft/profile";

	private MicrosoftAuth() {}

	public static final class Session {
		public final String name;
		public final String uuid;
		public final String accessToken;

		public Session(String name, String uuid, String accessToken) {
			this.name = name;
			this.uuid = uuid;
			this.accessToken = accessToken;
		}
	}

	/**
	 * Built-in client, unless {@code override} is a 16-hex Live client id.
	 * Azure application UUIDs are not accepted: they send the user nowhere useful.
	 */
	public static String resolveClientId(String override) {
		if (override != null) {
			String s = override.trim();
			if (s.matches("(?i)[0-9a-f]{16}")) {
				return s.toLowerCase();
			}
		}
		return BUILTIN_CLIENT_ID;
	}

	/**
	 * Opens a small sign-in window on Microsoft's page and returns a Minecraft session.
	 * {@code cancel} aborts the wait. Failures are plain sentences; this never asks
	 * the user to open Settings or to create an Azure application.
	 */
	public static Session signInWithBrowser(String clientOverride, Progress p, AtomicBoolean cancel) throws IOException {
		String clientId = resolveClientId(clientOverride);
		String url = authorizeUrl(clientId);
		File browser = findBrowser();
		if (browser != null) {
			p.log("Microsoft sign-in window (" + browser.getName() + ")");
			p.stage("Waiting for Microsoft sign-in");
			return signInWithDevtools(clientId, url, browser, p, cancel);
		}
		p.log("Microsoft sign-in in the default browser");
		p.stage("Waiting for Microsoft sign-in");
		return signInWithDefaultBrowser(clientId, url, p, cancel);
	}

	private static Session signInWithDevtools(String clientId, String url, File browser, Progress p, AtomicBoolean cancel) throws IOException {
		int port = freePort();
		File profile = new File(System.getProperty("java.io.tmpdir"), "c3h-ms-" + Long.toHexString(System.nanoTime()));
		if (!profile.mkdirs() && !profile.isDirectory()) {
			throw new IOException("Could not create a temporary folder for the sign-in window.");
		}
		Process proc = startBrowser(browser, url, port, profile);
		try {
			long deadline = System.currentTimeMillis() + 5L * 60L * 1000L;
			long debugDeadline = System.currentTimeMillis() + 20L * 1000L;
			boolean debugUp = false;
			while (!cancel.get() && System.currentTimeMillis() < deadline) {
				int livePort = debugPort(profile, port);
				String body = null;
				try {
					body = Http.getText("http://127.0.0.1:" + livePort + "/json");
					debugUp = true;
				} catch (IOException e) {
					if (!proc.isAlive() && !debugUp) {
						throw new IOException("The Microsoft sign-in window closed before it finished opening. Press Sign in to try again.");
					}
					if (!debugUp && System.currentTimeMillis() > debugDeadline) {
						throw new IOException("The sign-in window did not respond. Close it if it is still open, then press Sign in again.");
					}
					if (!proc.isAlive()) {
						throw new IOException("The sign-in window closed before Microsoft returned a login. Try again.");
					}
					sleep(250);
					continue;
				}
				Hit hit = scan(body);
				if (hit != null) {
					closeTarget(livePort, hit.id);
					return exchange(clientId, hit, p, cancel);
				}
				if (!proc.isAlive()) {
					throw new IOException("The sign-in window closed before Microsoft returned a login. Try again.");
				}
				sleep(300);
			}
			if (cancel.get()) {
				throw new IOException("Sign-in cancelled.");
			}
			throw new IOException("Microsoft sign-in timed out. Press Sign in to try again.");
		} finally {
			kill(proc);
			deleteTree(profile);
		}
	}

	/** Refresh the stored token and return a live Minecraft session. */
	public static Session ensure(String clientOverride, Progress p) throws IOException {
		String clientId = resolveClientId(clientOverride);
		AccountStore store = AccountStore.load();
		if (!store.signedIn()) {
			throw new IOException("Not signed in. Choose Microsoft on the home page and press Sign in with Microsoft.");
		}
		p.stage("Refreshing Microsoft login");
		MsToken ms;
		try {
			ms = token(refreshBody(clientId, store.refreshToken));
		} catch (Http.StatusException e) {
			throw new IOException(explainOauth(errorCode(e.body), e.body));
		}
		return finish(ms, p);
	}

	static String authorizeUrl(String clientId) {
		return AUTHORIZE + "?" + form(
				"client_id", clientId,
				"response_type", "code",
				"redirect_uri", REDIRECT,
				"scope", SCOPE);
	}

	static String codeTokenBody(String clientId, String code) {
		return form("client_id", clientId,
				"code", code,
				"grant_type", "authorization_code",
				"redirect_uri", REDIRECT,
				"scope", SCOPE);
	}

	static String refreshBody(String clientId, String refresh) {
		return form("client_id", clientId,
				"grant_type", "refresh_token",
				"refresh_token", refresh,
				"redirect_uri", REDIRECT,
				"scope", SCOPE);
	}

	/** MBI_SSL tickets are sent as-is. The Azure {@code d=} prefix is not used. */
	static String xblBody(String accessToken) {
		return "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\""
				+ jsonEscape(accessToken) + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
	}

	static String xstsBody(String xblToken) {
		return "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + jsonEscape(xblToken)
				+ "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
	}

	static String mcLoginBody(String uhs, String xsts) {
		return "{\"identityToken\":\"XBL3.0 x=" + jsonEscape(uhs) + ";" + jsonEscape(xsts) + "\"}";
	}

	static Hit hitFromUrl(String url) {
		if (!isDesktopRedirect(url)) {
			return null;
		}
		String code = queryParam(url, "code");
		String err = queryParam(url, "error");
		if ((code == null || code.isEmpty()) && (err == null || err.isEmpty())) {
			return null;
		}
		Hit h = new Hit();
		h.code = code;
		h.error = err;
		h.description = queryParam(url, "error_description");
		h.id = "";
		return h;
	}

	static boolean isDesktopRedirect(String url) {
		if (url == null || url.isEmpty()) {
			return false;
		}
		try {
			java.net.URL u = new java.net.URL(url);
			String host = u.getHost();
			String path = u.getPath();
			return host != null && host.equalsIgnoreCase("login.live.com")
					&& path != null && path.endsWith("/oauth20_desktop.srf");
		} catch (Exception e) {
			return false;
		}
	}

	static String queryParam(String url, String key) {
		if (url == null) {
			return null;
		}
		int q = url.indexOf('?');
		if (q < 0) {
			return null;
		}
		String query = url.substring(q + 1);
		int hash = query.indexOf('#');
		if (hash >= 0) {
			query = query.substring(0, hash);
		}
		String prefix = key + "=";
		for (String part : query.split("&")) {
			if (part.startsWith(prefix)) {
				try {
					return java.net.URLDecoder.decode(part.substring(prefix.length()), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return part.substring(prefix.length());
				}
			}
		}
		return null;
	}

	private static Session finish(MsToken ms, Progress p) throws IOException {
		p.stage("Xbox Live");
		String[] xb = xbox(ms.accessToken);
		p.stage("Minecraft services");
		String mc = minecraftToken(xb[0], xb[1]);
		p.stage("Minecraft profile");
		Session session = profile(mc);
		AccountStore store = new AccountStore();
		store.refreshToken = ms.refreshToken;
		store.name = session.name;
		store.uuid = session.uuid;
		store.save();
		return session;
	}

	private static final class MsToken {
		String accessToken;
		String refreshToken;
	}

	private static final class Hit {
		String code;
		String error;
		String description;
		String id;
	}

	private static MsToken token(String body) throws IOException {
		Map<String, Object> json = Json.obj(Json.parse(Http.post(TOKEN, "application/x-www-form-urlencoded", body, null)));
		MsToken t = new MsToken();
		t.accessToken = Json.str(json, "access_token");
		t.refreshToken = Json.str(json, "refresh_token");
		if (t.accessToken == null || t.refreshToken == null) {
			throw new IOException("Microsoft did not return a login token. Press Sign in and try again.");
		}
		return t;
	}

	/** @return {user hash, xsts token} */
	private static String[] xbox(String msAccess) throws IOException {
		Map<String, Object> xbl;
		try {
			xbl = Json.obj(Json.parse(Http.post(XBL, "application/json", xblBody(msAccess), null)));
		} catch (Http.StatusException e) {
			throw new IOException("Xbox Live rejected the Microsoft login. " + explainXErr(e.body));
		}
		String xblToken = Json.str(xbl, "Token");
		String uhs = userHash(xbl);
		if (xblToken == null || uhs == null) {
			throw new IOException("Xbox Live did not return a user token.");
		}
		Map<String, Object> xsts;
		try {
			xsts = Json.obj(Json.parse(Http.post(XSTS, "application/json", xstsBody(xblToken), null)));
		} catch (Http.StatusException e) {
			throw new IOException(explainXErr(e.body));
		}
		String xstsToken = Json.str(xsts, "Token");
		String uhs2 = userHash(xsts);
		if (uhs2 != null) {
			uhs = uhs2;
		}
		if (xstsToken == null || uhs == null) {
			throw new IOException("Xbox Live did not return an XSTS token.");
		}
		return new String[] { uhs, xstsToken };
	}

	private static String minecraftToken(String uhs, String xsts) throws IOException {
		Map<String, Object> json;
		try {
			json = Json.obj(Json.parse(Http.post(MC_LOGIN, "application/json", mcLoginBody(uhs, xsts), null)));
		} catch (Http.StatusException e) {
			throw new IOException("Minecraft services rejected the Xbox login (HTTP " + e.code + ").");
		}
		String token = Json.str(json, "access_token");
		if (token == null) {
			throw new IOException("Minecraft services did not return an access token.");
		}
		return token;
	}

	private static Session profile(String mcToken) throws IOException {
		Map<String, Object> json;
		try {
			json = Json.obj(Json.parse(Http.get(MC_PROFILE, "Bearer " + mcToken)));
		} catch (Http.StatusException e) {
			if (e.code == 404) {
				throw new IOException("This Microsoft account has no Minecraft Java profile. Buy the game or create the profile at minecraft.net, then sign in again.");
			}
			throw new IOException("Could not read the Minecraft profile (HTTP " + e.code + ").");
		}
		String name = Json.str(json, "name");
		String id = Json.str(json, "id");
		if (name == null || id == null) {
			throw new IOException("Minecraft profile was missing a name or UUID.");
		}
		return new Session(name, id.replace("-", ""), mcToken);
	}

	@SuppressWarnings("unchecked")
	static Hit scan(String body) {
		if (body == null || body.isEmpty()) {
			return null;
		}
		Object parsed;
		try {
			parsed = Json.parse(body);
		} catch (RuntimeException e) {
			return null;
		}
		if (!(parsed instanceof List)) {
			return null;
		}
		for (Object o : (List<?>) parsed) {
			if (!(o instanceof Map)) {
				continue;
			}
			Map<String, Object> m = (Map<String, Object>) o;
			Hit h = hitFromUrl(Json.str(m, "url"));
			if (h == null) {
				continue;
			}
			h.id = Json.str(m, "id");
			return h;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static String userHash(Map<String, Object> token) {
		Object claims = token.get("DisplayClaims");
		if (!(claims instanceof Map)) {
			return null;
		}
		Object xui = ((Map<String, Object>) claims).get("xui");
		if (!(xui instanceof List) || ((List<?>) xui).isEmpty()) {
			return null;
		}
		Object first = ((List<?>) xui).get(0);
		if (!(first instanceof Map)) {
			return null;
		}
		return Json.str((Map<String, Object>) first, "uhs");
	}

	private static String explainRedirect(String err, String description) {
		if ("authorization_declined".equals(err) || "access_denied".equals(err)) {
			return "Microsoft sign-in was declined.";
		}
		if (description != null && !description.isEmpty()) {
			String d = description;
			if (d.length() > 180) {
				d = d.substring(0, 180);
			}
			return "Microsoft login failed: " + d;
		}
		if (err != null && !err.isEmpty()) {
			return "Microsoft login failed (" + err + ").";
		}
		return "Microsoft login failed.";
	}

	private static String explainOauth(String err, String body) {
		if ("authorization_declined".equals(err) || "access_denied".equals(err)) {
			return "Microsoft sign-in was declined.";
		}
		if ("expired_token".equals(err)) {
			return "The Microsoft sign-in expired. Press Sign in and try again.";
		}
		if ("invalid_grant".equals(err) || "interaction_required".equals(err)) {
			return "The saved Microsoft session is no longer valid. Press Sign in and try again.";
		}
		String desc = errorDescription(body);
		if (desc != null && !desc.isEmpty()) {
			return "Microsoft login failed: " + desc;
		}
		if (err != null) {
			return "Microsoft login failed (" + err + ").";
		}
		return "Microsoft login failed.";
	}

	static String explainXErr(String body) {
		long code = xerr(body);
		if (code == 2148916233L) {
			return "This Microsoft account has no Xbox profile. Sign in at xbox.com once, then try again.";
		}
		if (code == 2148916238L) {
			return "This is a child account and must be added to a Family by an adult before Xbox Live will allow Minecraft.";
		}
		if (code == 2148916235L) {
			return "Xbox Live is not available for this account's country.";
		}
		if (code == 2148916236L || code == 2148916237L) {
			return "An adult must verify this account before it can play online.";
		}
		if (code > 0) {
			return "Xbox Live refused this Microsoft account (XErr " + code + ").";
		}
		return "Xbox Live refused this Microsoft account.";
	}

	private static long xerr(String body) {
		try {
			Object v = Json.parse(body);
			if (v instanceof Map) {
				return Json.num(Json.obj(v), "XErr");
			}
		} catch (RuntimeException ignored) {
		}
		return -1;
	}

	private static String errorCode(String body) {
		try {
			Object v = Json.parse(body);
			if (v instanceof Map) {
				return Json.str(Json.obj(v), "error");
			}
		} catch (RuntimeException ignored) {
		}
		return null;
	}

	private static String errorDescription(String body) {
		try {
			Object v = Json.parse(body);
			if (v instanceof Map) {
				String d = Json.str(Json.obj(v), "error_description");
				if (d != null && d.length() > 180) {
					d = d.substring(0, 180);
				}
				return d;
			}
		} catch (RuntimeException ignored) {
		}
		return null;
	}

	private static String form(String... kv) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < kv.length; i += 2) {
			if (b.length() > 0) {
				b.append('&');
			}
			b.append(url(kv[i])).append('=').append(url(kv[i + 1]));
		}
		return b.toString();
	}

	private static String url(String s) {
		try {
			return URLEncoder.encode(s == null ? "" : s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String jsonEscape(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static int freePort() throws IOException {
		ServerSocket ss = new ServerSocket();
		try {
			ss.setReuseAddress(true);
			ss.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0));
			return ss.getLocalPort();
		} finally {
			ss.close();
		}
	}

	private static int debugPort(File profile, int requested) {
		File f = new File(profile, "DevToolsActivePort");
		if (!f.isFile()) {
			return requested;
		}
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			try {
				String line = r.readLine();
				if (line != null) {
					int p = Integer.parseInt(line.trim());
					if (p > 0) {
						return p;
					}
				}
			} finally {
				r.close();
			}
		} catch (Exception ignored) {
		}
		return requested;
	}

	private static final String TAB_SCRIPT =
			"on pageUrl(appName)\n"
			+ "  try\n"
			+ "    tell application appName\n"
			+ "      if (count of windows) is 0 then return \"\"\n"
			+ "      repeat with w in windows\n"
			+ "        try\n"
			+ "          repeat with t in tabs of w\n"
			+ "            try\n"
			+ "              set u to URL of t as text\n"
			+ "              if u contains \"oauth20_desktop.srf\" then return u\n"
			+ "            end try\n"
			+ "          end repeat\n"
			+ "        end try\n"
			+ "      end repeat\n"
			+ "    end tell\n"
			+ "  end try\n"
			+ "  return \"\"\n"
			+ "end pageUrl\n"
			+ "set browsers to {\"Safari\", \"Google Chrome\", \"Microsoft Edge\", \"Brave Browser\", \"Chromium\", \"Opera\", \"Vivaldi\"}\n"
			+ "repeat with b in browsers\n"
			+ "  set u to pageUrl(b as text)\n"
			+ "  if u is not \"\" then return u\n"
			+ "end repeat\n"
			+ "return \"\"\n";

	private static final String ADDRESS_PS1 =
			"$ErrorActionPreference = 'SilentlyContinue'\r\n"
			+ "Add-Type -AssemblyName UIAutomationClient\r\n"
			+ "$root = [System.Windows.Automation.AutomationElement]::RootElement\r\n"
			+ "$cond = New-Object System.Windows.Automation.PropertyCondition ([System.Windows.Automation.AutomationElement]::ControlTypeProperty, [System.Windows.Automation.ControlType]::Edit)\r\n"
			+ "$all = $root.FindAll([System.Windows.Automation.TreeScope]::Descendants, $cond)\r\n"
			+ "foreach ($e in $all) {\r\n"
			+ "  $id = [string]$e.Current.AutomationId\r\n"
			+ "  $name = [string]$e.Current.Name\r\n"
			+ "  $ok = $id -eq 'addressEditBox' -or $id -eq 'urlbar-input' -or $id -eq 'omnibox' -or $name -like '*address*' -or $name -like '*Address*' -or $name -like '*indirizz*'\r\n"
			+ "  if (-not $ok) { continue }\r\n"
			+ "  try {\r\n"
			+ "    $vp = $e.GetCurrentPattern([System.Windows.Automation.ValuePattern]::Pattern)\r\n"
			+ "    $v = [string]$vp.Current.Value\r\n"
			+ "    if ($v -like '*oauth20_desktop.srf*') { Write-Output $v }\r\n"
			+ "  } catch {}\r\n"
			+ "}\r\n";

	private static Session exchange(String clientId, Hit hit, Progress p, AtomicBoolean cancel) throws IOException {
		if (hit.error != null && (hit.code == null || hit.code.isEmpty())) {
			throw new IOException(explainRedirect(hit.error, hit.description));
		}
		if (cancel.get()) {
			throw new IOException("Sign-in cancelled.");
		}
		MsToken ms;
		try {
			ms = token(codeTokenBody(clientId, hit.code));
		} catch (Http.StatusException e) {
			throw new IOException(explainOauth(errorCode(e.body), e.body));
		}
		if (cancel.get()) {
			throw new IOException("Sign-in cancelled.");
		}
		return finish(ms, p);
	}

	/** Same authorize URL and client id. The page opens in whatever browser the OS uses. */
	private static Session signInWithDefaultBrowser(String clientId, String url, Progress p, AtomicBoolean cancel) throws IOException {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("mac")) {
			return signInMacBrowser(clientId, url, p, cancel);
		}
		if (os.contains("win")) {
			return signInWindowsBrowser(clientId, url, p, cancel);
		}
		File firefox = onPath("firefox");
		if (firefox == null) {
			firefox = onPath("firefox-esr");
		}
		if (firefox != null) {
			p.log("Microsoft sign-in window (" + firefox.getName() + ")");
			return signInFirefox(clientId, url, firefox, p, cancel);
		}
		throw new IOException("Could not open the Microsoft sign-in page. No Edge, Chrome, Chromium, or Firefox was found, and the default browser cannot be read. Press Sign in to try again.");
	}

	private static Session signInMacBrowser(String clientId, String url, Progress p, AtomicBoolean cancel) throws IOException {
		Process open = new ProcessBuilder("open", url).redirectErrorStream(true).start();
		drain(open);
		waitFor(open, 15);
		if (open.exitValue() != 0) {
			throw new IOException("Could not open the Microsoft sign-in page in the default browser. Press Sign in to try again.");
		}
		long deadline = System.currentTimeMillis() + 5L * 60L * 1000L;
		while (!cancel.get() && System.currentTimeMillis() < deadline) {
			String tab = osascript(TAB_SCRIPT);
			Hit hit = hitFromUrl(tab == null ? null : tab.trim());
			if (hit != null) {
				return exchange(clientId, hit, p, cancel);
			}
			sleep(400);
		}
		if (cancel.get()) {
			throw new IOException("Sign-in cancelled.");
		}
		throw new IOException("Microsoft sign-in timed out. If macOS asked to control the browser, allow Confu Client and press Sign in again.");
	}

	private static Session signInWindowsBrowser(String clientId, String url, Progress p, AtomicBoolean cancel) throws IOException {
		Process open = new ProcessBuilder("cmd.exe", "/c", "start \"\" \"" + url + "\"").redirectErrorStream(true).start();
		drain(open);
		File ps1 = new File(System.getProperty("java.io.tmpdir"), "c3h-ms-" + Long.toHexString(System.nanoTime()) + ".ps1");
		FileOutputStream fos = new FileOutputStream(ps1);
		try {
			fos.write(ADDRESS_PS1.getBytes("UTF-8"));
		} finally {
			fos.close();
		}
		try {
			long deadline = System.currentTimeMillis() + 5L * 60L * 1000L;
			while (!cancel.get() && System.currentTimeMillis() < deadline) {
				String listed = powershell(ps1);
				Hit hit = hitFromUrl(firstDesktopUrl(listed));
				if (hit != null) {
					return exchange(clientId, hit, p, cancel);
				}
				sleep(1000);
			}
		} finally {
			ps1.delete();
		}
		if (cancel.get()) {
			throw new IOException("Sign-in cancelled.");
		}
		throw new IOException("Microsoft sign-in timed out. Press Sign in to try again.");
	}

	private static Session signInFirefox(String clientId, String url, File firefox, Progress p, AtomicBoolean cancel) throws IOException {
		int port = freePort();
		File profile = new File(System.getProperty("java.io.tmpdir"), "c3h-ms-" + Long.toHexString(System.nanoTime()));
		if (!profile.mkdirs() && !profile.isDirectory()) {
			throw new IOException("Could not create a temporary folder for the sign-in window.");
		}
		List<String> cmd = new ArrayList<String>();
		cmd.add(firefox.getAbsolutePath());
		cmd.add("--new-instance");
		cmd.add("--profile");
		cmd.add(profile.getAbsolutePath());
		cmd.add("--remote-debugging-port=" + port);
		cmd.add("--no-first-run");
		cmd.add("--no-default-browser-check");
		cmd.add(url);
		Process proc = startRaw(cmd);
		try {
			long deadline = System.currentTimeMillis() + 5L * 60L * 1000L;
			long debugDeadline = System.currentTimeMillis() + 25L * 1000L;
			boolean debugUp = false;
			while (!cancel.get() && System.currentTimeMillis() < deadline) {
				String body = null;
				try {
					body = Http.getText("http://127.0.0.1:" + port + "/json/list");
					debugUp = true;
				} catch (IOException e) {
					try {
						body = Http.getText("http://127.0.0.1:" + port + "/json");
						debugUp = true;
					} catch (IOException e2) {
						if (!debugUp && System.currentTimeMillis() > debugDeadline && !proc.isAlive()) {
							throw new IOException("The sign-in window closed before it finished opening. Press Sign in to try again.");
						}
						sleep(300);
						continue;
					}
				}
				Hit hit = scan(body);
				if (hit != null) {
					return exchange(clientId, hit, p, cancel);
				}
				if (!proc.isAlive() && debugUp) {
					throw new IOException("The sign-in window closed before Microsoft returned a login. Try again.");
				}
				sleep(300);
			}
			if (cancel.get()) {
				throw new IOException("Sign-in cancelled.");
			}
			throw new IOException("Microsoft sign-in timed out. Press Sign in to try again.");
		} finally {
			kill(proc);
			deleteTree(profile);
		}
	}

	private static File findBrowser() {
		String os = System.getProperty("os.name", "").toLowerCase();
		List<File> cands = new ArrayList<File>();
		if (os.contains("win")) {
			String pf = env("ProgramFiles");
			String pf86 = env("ProgramFiles(x86)");
			String local = env("LOCALAPPDATA");
			String[] rels = new String[] {
				"Microsoft\\Edge\\Application\\msedge.exe",
				"Google\\Chrome\\Application\\chrome.exe",
				"BraveSoftware\\Brave-Browser\\Application\\brave.exe",
				"Chromium\\Application\\chrome.exe",
				"Vivaldi\\Application\\vivaldi.exe",
				"Opera Software\\Opera Stable\\opera.exe"
			};
			for (int i = 0; i < rels.length; i++) {
				add(cands, pf, rels[i]);
				add(cands, pf86, rels[i]);
				add(cands, local, rels[i]);
			}
		} else if (os.contains("mac")) {
			String home = System.getProperty("user.home", "");
			String[] apps = new String[] {
				"Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
				"Google Chrome.app/Contents/MacOS/Google Chrome",
				"Brave Browser.app/Contents/MacOS/Brave Browser",
				"Chromium.app/Contents/MacOS/Chromium",
				"Vivaldi.app/Contents/MacOS/Vivaldi",
				"Opera.app/Contents/MacOS/Opera"
			};
			for (int i = 0; i < apps.length; i++) {
				cands.add(new File("/Applications", apps[i]));
				if (!home.isEmpty()) {
					cands.add(new File(new File(home, "Applications"), apps[i]));
				}
			}
		}
		for (int i = 0; i < cands.size(); i++) {
			if (executable(cands.get(i))) {
				return cands.get(i);
			}
		}
		String[] names = os.contains("win")
				? new String[] { "msedge.exe", "chrome.exe", "brave.exe", "vivaldi.exe", "opera.exe" }
				: new String[] {
					"microsoft-edge", "microsoft-edge-stable", "google-chrome", "google-chrome-stable",
					"chromium", "chromium-browser", "brave-browser", "brave", "vivaldi", "vivaldi-stable", "opera"
				};
		for (int i = 0; i < names.length; i++) {
			File f = onPath(names[i]);
			if (f != null) {
				return f;
			}
		}
		if (!os.contains("win") && !os.contains("mac")) {
			return linuxDefaultChromium();
		}
		return null;
	}

	private static void add(List<File> cands, String root, String rel) {
		if (root != null && !root.isEmpty()) {
			cands.add(new File(root, rel));
		}
	}

	private static String env(String key) {
		String v = System.getenv(key);
		return v == null ? "" : v;
	}

	private static File onPath(String name) {
		String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}
		String[] dirs = path.split(File.pathSeparator);
		for (int i = 0; i < dirs.length; i++) {
			File f = new File(dirs[i], name);
			if (executable(f)) {
				return f;
			}
		}
		return null;
	}

	private static boolean executable(File f) {
		if (f == null || !f.isFile()) {
			return false;
		}
		String os = System.getProperty("os.name", "").toLowerCase();
		return os.contains("win") || f.canExecute();
	}

	private static Process startBrowser(File browser, String url, int port, File profile) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add(browser.getAbsolutePath());
		cmd.add("--app=" + url);
		cmd.add("--remote-debugging-port=" + port);
		cmd.add("--user-data-dir=" + profile.getAbsolutePath());
		cmd.add("--no-first-run");
		cmd.add("--no-default-browser-check");
		cmd.add("--disable-sync");
		cmd.add("--disable-extensions");
		cmd.add("--disable-popup-blocking");
		cmd.add("--disable-translate");
		cmd.add("--remote-allow-origins=*");
		cmd.add("--window-size=480,640");
		return startRaw(cmd);
	}

	private static Process startRaw(List<String> cmd) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		final Process proc = pb.start();
		drain(proc);
		return proc;
	}

	private static void drain(final Process proc) {
		Thread drain = new Thread(new Runnable() {
			public void run() {
				InputStream in = proc.getInputStream();
				byte[] buf = new byte[2048];
				try {
					while (in.read(buf) >= 0) {
						// discard; a full pipe would stall the window
					}
				} catch (IOException ignored) {
				}
			}
		}, "ms-login-browser");
		drain.setDaemon(true);
		drain.start();
	}

	private static void waitFor(Process proc, int seconds) throws IOException {
		try {
			if (!proc.waitFor(seconds, TimeUnit.SECONDS)) {
				proc.destroy();
				throw new IOException("Could not open the Microsoft sign-in page in the default browser. Press Sign in to try again.");
			}
		} catch (InterruptedException e) {
			proc.destroy();
			Thread.currentThread().interrupt();
			throw new IOException("Sign-in cancelled.");
		}
	}

	private static String osascript(String script) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("osascript", "-");
		Process proc = pb.start();
		OutputStream out = proc.getOutputStream();
		out.write(script.getBytes("UTF-8"));
		out.close();
		String stdout = readAll(proc.getInputStream());
		String stderr = readAll(proc.getErrorStream());
		try {
			if (!proc.waitFor(45, TimeUnit.SECONDS)) {
				proc.destroyForcibly();
				return "";
			}
		} catch (InterruptedException e) {
			proc.destroyForcibly();
			Thread.currentThread().interrupt();
			throw new IOException("Sign-in cancelled.");
		}
		boolean blocked = stderr != null && (stderr.indexOf("not allowed") >= 0 || stderr.indexOf("Not authorized") >= 0 || stderr.indexOf("-1743") >= 0);
		if (blocked && (stdout == null || stdout.trim().isEmpty())) {
			throw new IOException("macOS blocked Confu Client from reading the sign-in page. Allow Confu Client to control the browser in System Settings, Privacy and Security, Automation, then press Sign in again.");
		}
		return stdout;
	}

	private static String powershell(File ps1) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(
				"powershell.exe", "-NoProfile", "-NonInteractive", "-STA",
				"-ExecutionPolicy", "Bypass", "-File", ps1.getAbsolutePath());
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		String out = readAll(proc.getInputStream());
		try {
			if (!proc.waitFor(20, TimeUnit.SECONDS)) {
				proc.destroyForcibly();
			}
		} catch (InterruptedException e) {
			proc.destroyForcibly();
			Thread.currentThread().interrupt();
			throw new IOException("Sign-in cancelled.");
		}
		return out;
	}

	private static String firstDesktopUrl(String text) {
		if (text == null) {
			return null;
		}
		int at = text.indexOf("https://login.live.com/oauth20_desktop.srf");
		if (at < 0) {
			at = text.indexOf("http://login.live.com/oauth20_desktop.srf");
		}
		if (at < 0) {
			return null;
		}
		int end = at;
		while (end < text.length()) {
			char c = text.charAt(end);
			if (c == '\n' || c == '\r' || c == ' ' || c == '"' || c == '\'') {
				break;
			}
			end++;
		}
		return text.substring(at, end);
	}

	private static String readAll(InputStream in) throws IOException {
		byte[] buf = new byte[4096];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int n;
		try {
			while ((n = in.read(buf)) >= 0) {
				out.write(buf, 0, n);
			}
		} finally {
			in.close();
		}
		return new String(out.toByteArray(), "UTF-8");
	}

	/** Chromium named by the desktop file xdg-settings points at, or null. */
	private static File linuxDefaultChromium() {
		String desktop;
		try {
			Process proc = new ProcessBuilder("xdg-settings", "get", "default-web-browser").redirectErrorStream(true).start();
			String out = readAll(proc.getInputStream());
			if (!proc.waitFor(3, TimeUnit.SECONDS)) {
				proc.destroy();
				return null;
			}
			desktop = out.trim();
		} catch (Exception e) {
			return null;
		}
		if (desktop.length() < 9 || !desktop.endsWith(".desktop") || desktop.indexOf('/') >= 0) {
			return null;
		}
		String home = System.getProperty("user.home", "");
		String[] roots = new String[] {
			"/usr/share/applications/",
			"/usr/local/share/applications/",
			home + "/.local/share/applications/",
			"/var/lib/flatpak/exports/share/applications/",
			home + "/.local/share/flatpak/exports/share/applications/"
		};
		for (int i = 0; i < roots.length; i++) {
			File f = new File(roots[i] + desktop);
			if (!f.isFile()) {
				continue;
			}
			String exec = desktopExec(f);
			if (exec == null) {
				continue;
			}
			String bin = exec.trim().split("\\s+")[0];
			if (bin.length() == 0 || bin.charAt(0) == '%') {
				continue;
			}
			File file = bin.charAt(0) == '/' ? new File(bin) : onPath(bin);
			if (file != null && chromiumFile(file)) {
				return file;
			}
		}
		return null;
	}

	private static String desktopExec(File desktop) {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(desktop), "UTF-8"));
			try {
				String line;
				while ((line = r.readLine()) != null) {
					if (line.startsWith("Exec=")) {
						return line.substring(5).replace("%u", "").replace("%U", "").replace("%f", "").replace("%F", "").trim();
					}
				}
			} finally {
				r.close();
			}
		} catch (IOException ignored) {
		}
		return null;
	}

	private static boolean chromiumFile(File f) {
		String n = f.getName().toLowerCase();
		return n.indexOf("chrome") >= 0 || n.indexOf("chromium") >= 0 || n.indexOf("msedge") >= 0
				|| n.indexOf("edge") >= 0 || n.indexOf("brave") >= 0 || n.indexOf("vivaldi") >= 0;
	}

	private static void closeTarget(int port, String id) {
		if (id == null || id.isEmpty()) {
			return;
		}
		try {
			Http.getText("http://127.0.0.1:" + port + "/json/close/" + id);
		} catch (IOException ignored) {
		}
	}

	private static void kill(Process proc) {
		if (proc == null) {
			return;
		}
		proc.destroy();
		try {
			if (!proc.waitFor(2, TimeUnit.SECONDS)) {
				proc.destroyForcibly();
				proc.waitFor(2, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			proc.destroyForcibly();
			Thread.currentThread().interrupt();
		}
	}

	private static void deleteTree(File f) {
		if (f == null || !f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) {
				for (int i = 0; i < kids.length; i++) {
					deleteTree(kids[i]);
				}
			}
		}
		f.delete();
	}

	private static void sleep(long ms) throws IOException {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Sign-in cancelled.");
		}
	}

	/** Offline checks of the request chain. {@code --online} also calls Microsoft and Xbox with a fake token. {@code --window} opens Chrome on example.com and reads the page URL back. */
	public static void main(String[] args) throws Exception {
		checkInvariants();
		System.out.println("offline chain checks: ok");
		for (int i = 0; i < args.length; i++) {
			if ("--online".equals(args[i])) {
				onlineProbe();
			} else if ("--window".equals(args[i])) {
				windowProbe();
			}
		}
	}

	private static void checkInvariants() {
		if (!BUILTIN_CLIENT_ID.equals(resolveClientId(null))) fail("null override");
		if (!BUILTIN_CLIENT_ID.equals(resolveClientId(""))) fail("empty override");
		if (!BUILTIN_CLIENT_ID.equals(resolveClientId("  "))) fail("blank override");
		if (!BUILTIN_CLIENT_ID.equals(resolveClientId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))) fail("azure uuid must be ignored");
		if (!"00000000402b5328".equals(resolveClientId("00000000402B5328"))) fail("hex override");

		String auth = authorizeUrl(BUILTIN_CLIENT_ID);
		if (!auth.startsWith(AUTHORIZE + "?")) fail("authorize host");
		if (!auth.contains("client_id=" + BUILTIN_CLIENT_ID)) fail("authorize client");
		if (!auth.contains("response_type=code")) fail("authorize type");
		if (!auth.contains("redirect_uri=")) fail("authorize redirect missing");
		if (!auth.contains("oauth20_desktop.srf")) fail("authorize redirect value");
		if (!auth.contains("MBI_SSL")) fail("authorize scope");
		if (auth.contains("login.microsoftonline.com")) fail("authorize must not be azure");
		if (isDesktopRedirect(auth)) fail("authorize url must not look like the desktop redirect");

		String desktop = REDIRECT + "?code=M.C507-test&lc=1033";
		if (!isDesktopRedirect(desktop)) fail("desktop redirect");
		if (!"M.C507-test".equals(queryParam(desktop, "code"))) fail("code parse");
		Hit direct = hitFromUrl(desktop);
		if (direct == null || !"M.C507-test".equals(direct.code)) fail("hitFromUrl code");
		if (hitFromUrl(auth) != null) fail("hitFromUrl must ignore the authorize url");
		String denied = REDIRECT + "?error=access_denied&error_description=The%20user%20cancelled";
		Hit directDenied = hitFromUrl(denied);
		if (directDenied == null || !"access_denied".equals(directDenied.error)) fail("hitFromUrl error");
		if (!"access_denied".equals(queryParam(denied, "error"))) fail("error parse");
		if (!"The user cancelled".equals(queryParam(denied, "error_description"))) fail("error description");

		String list = "[{\"type\":\"page\",\"id\":\"abc\",\"url\":\"" + auth.replace("\"", "") + "\"},"
				+ "{\"type\":\"page\",\"id\":\"def\",\"url\":\"" + desktop + "\"}]";
		Hit hit = scan(list);
		if (hit == null || !"M.C507-test".equals(hit.code) || !"def".equals(hit.id)) fail("scan code: " + (hit == null ? "null" : hit.code));
		Hit bad = scan("[{\"type\":\"page\",\"id\":\"z\",\"url\":\"" + denied + "\"}]");
		if (bad == null || !"access_denied".equals(bad.error)) fail("scan error");
		if (scan("[{\"type\":\"page\",\"id\":\"z\",\"url\":\"https://login.live.com/oauth20_authorize.srf\"}]") != null) fail("scan authorize");

		String tokenBody = codeTokenBody(BUILTIN_CLIENT_ID, "M.C507-test");
		if (!tokenBody.contains("grant_type=authorization_code")) fail("token grant");
		if (!tokenBody.contains("client_id=" + BUILTIN_CLIENT_ID)) fail("token client");
		if (!tokenBody.contains("code=M.C507-test")) fail("token code");
		if (!tokenBody.contains("redirect_uri=")) fail("token redirect");
		String refresh = refreshBody(BUILTIN_CLIENT_ID, "R.token");
		if (!refresh.contains("grant_type=refresh_token")) fail("refresh grant");
		if (!refresh.contains("refresh_token=R.token")) fail("refresh token");

		String xbl = xblBody("EwA4example");
		if (xbl.contains("\"RpsTicket\":\"d=")) fail("MBI_SSL must not use the d= prefix: " + xbl);
		if (!xbl.contains("\"RpsTicket\":\"EwA4example\"")) fail("rps ticket");
		if (!xbl.contains("\"RelyingParty\":\"http://auth.xboxlive.com\"")) fail("xbl rp");
		if (!xbl.contains("\"AuthMethod\":\"RPS\"")) fail("xbl method");
		if (!xbl.contains("\"SiteName\":\"user.auth.xboxlive.com\"")) fail("xbl site");
		if (!xbl.contains("\"TokenType\":\"JWT\"")) fail("xbl jwt");
		Json.obj(Json.parse(xbl));

		String xsts = xstsBody("xbl-token");
		if (!xsts.contains("\"SandboxId\":\"RETAIL\"")) fail("sandbox");
		if (!xsts.contains("\"RelyingParty\":\"rp://api.minecraftservices.com/\"")) fail("xsts rp");
		if (!xsts.contains("\"UserTokens\":[\"xbl-token\"]")) fail("user tokens");
		Json.obj(Json.parse(xsts));

		String mc = mcLoginBody("1234567890", "xsts-token");
		if (!mc.contains("\"identityToken\":\"XBL3.0 x=1234567890;xsts-token\"")) fail("identity: " + mc);
		Json.obj(Json.parse(mc));

		if (!explainXErr("{\"XErr\":2148916233}").contains("no Xbox profile")) fail("xerr profile");
		if (!explainXErr("{\"XErr\":2148916238}").contains("child")) fail("xerr child");
		if (!explainXErr("{\"XErr\":2148916235}").contains("country")) fail("xerr country");
		if (!explainXErr("{\"XErr\":2148916236}").contains("adult")) fail("xerr adult");
		if (!explainXErr("{\"XErr\":2148916237}").contains("adult")) fail("xerr adult 2");
		String generic = explainXErr("{\"XErr\":1}");
		if (generic.contains("Azure") || generic.contains("Settings") || !generic.contains("XErr 1")) fail("xerr generic: " + generic);
		if (explainXErr("not-json").contains("Azure") || explainXErr("not-json").contains("Settings")) fail("xerr fallback");
	}

	private static void onlineProbe() throws IOException {
		String auth = authorizeUrl(BUILTIN_CLIENT_ID);
		String page = Http.getText(auth);
		if (page == null || page.length() < 200) fail("authorize page too short");
		String lower = page.toLowerCase();
		if (lower.contains("invalid_client") || lower.contains("client_id is invalid")) fail("live rejected the built-in client");
		System.out.println("authorize page: " + page.length() + " bytes");

		try {
			Http.post(TOKEN, "application/x-www-form-urlencoded", codeTokenBody(BUILTIN_CLIENT_ID, "not-a-real-code"), null);
			fail("token endpoint accepted a fake code");
		} catch (Http.StatusException e) {
			String err = errorCode(e.body);
			if ("invalid_client".equals(err) || "unauthorized_client".equals(err)) {
				fail("token endpoint rejected the client: " + e.body);
			}
			System.out.println("token endpoint HTTP " + e.code + " error=" + err);
		}

		try {
			Http.post(XBL, "application/json", xblBody("EwA4example-not-a-real-token"), null);
			fail("xbox accepted a fake ticket");
		} catch (Http.StatusException e) {
			if (e.code == 400 && e.body != null && e.body.toLowerCase().contains("json")) {
				fail("xbox rejected the JSON body: " + e.body);
			}
			System.out.println("xbox HTTP " + e.code + " (" + e.body.length() + " bytes)");
		}
		System.out.println("online chain checks: ok");
	}

	private static void windowProbe() throws Exception {
		File browser = findBrowser();
		if (browser == null) fail("no browser");
		int port = freePort();
		File profile = new File(System.getProperty("java.io.tmpdir"), "c3h-ms-probe-" + Long.toHexString(System.nanoTime()));
		profile.mkdirs();
		Process proc = startBrowser(browser, "https://example.com/", port, profile);
		try {
			long deadline = System.currentTimeMillis() + 25L * 1000L;
			boolean seen = false;
			while (System.currentTimeMillis() < deadline) {
				int live = debugPort(profile, port);
				try {
					String body = Http.getText("http://127.0.0.1:" + live + "/json");
					if (body.contains("example.com")) {
						seen = true;
						System.out.println("window probe: page url visible on port " + live);
						break;
					}
				} catch (IOException ignored) {
				}
				if (!proc.isAlive()) fail("browser exited before the debug port came up");
				sleep(300);
			}
			if (!seen) fail("debug list never showed example.com");
		} finally {
			kill(proc);
			deleteTree(profile);
		}
	}

	private static void fail(String msg) {
		throw new AssertionError(msg);
	}
}
