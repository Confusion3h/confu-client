package confusione.launcher.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Microsoft device-code login for Minecraft Java: Microsoft OAuth, Xbox Live, XSTS,
 * Minecraft services, profile. The Azure application (client) ID is a setting.
 * There is no client secret; public clients use the device-code grant.
 */
public final class MicrosoftAuth {

	static final String SCOPE = "XboxLive.signin offline_access";
	private static final String DEVICE = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
	private static final String TOKEN = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
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

	public static final class Device {
		public String userCode;
		public String deviceCode;
		public String verificationUri;
		public String message;
		public int interval;
		public long expiresAt;
	}

	public static boolean validClientId(String id) {
		return id != null && id.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
	}

	public static Device start(String clientId) throws IOException {
		requireClient(clientId);
		Map<String, Object> json = Json.obj(Json.parse(Http.post(DEVICE, "application/x-www-form-urlencoded",
				form("client_id", clientId, "scope", SCOPE), null)));
		Device d = new Device();
		d.userCode = Json.str(json, "user_code");
		d.deviceCode = Json.str(json, "device_code");
		d.verificationUri = Json.str(json, "verification_uri");
		if (d.verificationUri == null || d.verificationUri.isEmpty()) {
			d.verificationUri = "https://www.microsoft.com/link";
		}
		d.message = Json.str(json, "message");
		long interval = Json.num(json, "interval");
		d.interval = (int) (interval < 1 ? 5 : interval);
		long exp = Json.num(json, "expires_in");
		d.expiresAt = System.currentTimeMillis() + (exp < 1 ? 900 : exp) * 1000L;
		if (d.userCode == null || d.deviceCode == null) {
			throw new IOException("Microsoft did not return a device code.");
		}
		return d;
	}

	/** @return null while the user has not finished; throws on denial or expiry */
	public static Session poll(String clientId, Device d, Progress p) throws IOException {
		try {
			MsToken ms = token(form("client_id", clientId,
					"grant_type", "urn:ietf:params:oauth:grant-type:device_code",
					"device_code", d.deviceCode));
			return finish(ms, p);
		} catch (Http.StatusException e) {
			String err = errorCode(e.body);
			if ("authorization_pending".equals(err)) {
				return null;
			}
			if ("slow_down".equals(err)) {
				d.interval += 5;
				return null;
			}
			throw new IOException(explainOauth(err, e.body));
		}
	}

	/** Refresh the stored token and return a live Minecraft session. */
	public static Session ensure(String clientId, Progress p) throws IOException {
		requireClient(clientId);
		AccountStore store = AccountStore.load();
		if (!store.signedIn()) {
			throw new IOException("Not signed in. Choose Microsoft on the home page and sign in.");
		}
		p.stage("Refreshing Microsoft login");
		MsToken ms;
		try {
			ms = token(form("client_id", clientId, "grant_type", "refresh_token",
					"refresh_token", store.refreshToken, "scope", SCOPE));
		} catch (Http.StatusException e) {
			throw new IOException(explainOauth(errorCode(e.body), e.body));
		}
		return finish(ms, p);
	}

	private static void requireClient(String clientId) throws IOException {
		if (!validClientId(clientId)) {
			throw new IOException("Set a Microsoft application (client) ID in Settings. See docs/microsoft-login.md.");
		}
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

	private static MsToken token(String body) throws IOException {
		Map<String, Object> json = Json.obj(Json.parse(Http.post(TOKEN, "application/x-www-form-urlencoded", body, null)));
		MsToken t = new MsToken();
		t.accessToken = Json.str(json, "access_token");
		t.refreshToken = Json.str(json, "refresh_token");
		if (t.accessToken == null || t.refreshToken == null) {
			throw new IOException("Microsoft token response was missing tokens.");
		}
		return t;
	}

	/** @return {user hash, xsts token} */
	private static String[] xbox(String msAccess) throws IOException {
		String xblBody = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d="
				+ jsonEscape(msAccess) + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
		Map<String, Object> xbl;
		try {
			xbl = Json.obj(Json.parse(Http.post(XBL, "application/json", xblBody, null)));
		} catch (Http.StatusException e) {
			throw new IOException("Xbox Live rejected the Microsoft login. " + explainXErr(e.body));
		}
		String xblToken = Json.str(xbl, "Token");
		String uhs = userHash(xbl);
		String xstsBody = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + jsonEscape(xblToken)
				+ "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
		Map<String, Object> xsts;
		try {
			xsts = Json.obj(Json.parse(Http.post(XSTS, "application/json", xstsBody, null)));
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
		String body = "{\"identityToken\":\"XBL3.0 x=" + jsonEscape(uhs) + ";" + jsonEscape(xsts) + "\"}";
		Map<String, Object> json;
		try {
			json = Json.obj(Json.parse(Http.post(MC_LOGIN, "application/json", body, null)));
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
	private static String userHash(Map<String, Object> token) {
		Object claims = token.get("DisplayClaims");
		if (!(claims instanceof Map)) {
			return null;
		}
		Object xui = ((Map<String, Object>) claims).get("xui");
		if (!(xui instanceof java.util.List) || ((java.util.List<?>) xui).isEmpty()) {
			return null;
		}
		Object first = ((java.util.List<?>) xui).get(0);
		if (!(first instanceof Map)) {
			return null;
		}
		return Json.str((Map<String, Object>) first, "uhs");
	}

	private static String explainOauth(String err, String body) {
		if ("authorization_declined".equals(err) || "access_denied".equals(err)) {
			return "Microsoft sign-in was declined.";
		}
		if ("expired_token".equals(err)) {
			return "The sign-in code expired. Try again.";
		}
		if ("invalid_grant".equals(err) || "interaction_required".equals(err)) {
			return "The saved Microsoft session is no longer valid. Sign in again.";
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

	private static String explainXErr(String body) {
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
			return "Xbox Live refused the account (XErr " + code + "). A custom Azure app is often rejected here until Microsoft allows that client ID.";
		}
		return "Xbox Live refused the account. A custom Azure app is often rejected until Microsoft allows that client ID.";
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
}
