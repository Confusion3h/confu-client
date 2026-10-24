package me.eldodebug.soar.management.music;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Spotify Web API remote control (the user's Spotify app plays the audio; this only
 * sends commands and reads state). Authorization Code with PKCE: the user supplies
 * their own Client ID, no client secret exists anywhere. Tokens (never the ID) are
 * kept in a properties file next to the client's other data.
 *
 * Uses only JDK HTTP + the Gson that Minecraft already ships.
 */
public final class SpotifyClient {

	public static final int PORT = 8888;
	public static final String REDIRECT_URI = "http://127.0.0.1:" + PORT + "/callback";
	static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-currently-playing";
	static final String ACCOUNTS = "https://accounts.spotify.com";
	static final String API = "https://api.spotify.com/v1";

	private final File tokenFile;
	private String clientId;
	private String accessToken;
	private String refreshToken;
	private long expiresAt;
	private String verifier;
	private String state;
	private HttpServer server;

	public SpotifyClient(File tokenFile) {
		this.tokenFile = tokenFile;
		load();
	}

	public void setClientId(String id) {
		String clean = id == null ? "" : id.trim();
		if (!clean.equals(clientId)) {
			clientId = clean;
		}
	}

	public boolean hasClientId() {
		return clientId != null && clientId.matches("[0-9a-zA-Z]{16,64}");
	}

	public boolean isAuthorized() {
		return refreshToken != null;
	}

	// ---- OAuth (PKCE) ---------------------------------------------------------

	/** Creates a fresh verifier/state and returns the URL to open in the browser. */
	public String beginAuthorization() throws IOException {
		if (!hasClientId()) {
			throw new IOException("Set your Spotify Client ID first");
		}
		verifier = randomUrlSafe(64);
		state = randomUrlSafe(16);
		startCallbackServer();
		return ACCOUNTS + "/authorize?response_type=code"
				+ "&client_id=" + enc(clientId)
				+ "&redirect_uri=" + enc(REDIRECT_URI)
				+ "&scope=" + enc(SCOPES)
				+ "&code_challenge_method=S256"
				+ "&code_challenge=" + challenge(verifier)
				+ "&state=" + state;
	}

	public interface AuthCallback {
		void done(boolean ok, String message);
	}

	private volatile AuthCallback authCallback;

	public void setAuthCallback(AuthCallback cb) {
		authCallback = cb;
	}

	private synchronized void startCallbackServer() throws IOException {
		if (server != null) {
			return;
		}
		// loopback only; Spotify requires the 127.0.0.1 literal for http redirects
		server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), 0);
		server.createContext("/callback", new HttpHandler() {
			public void handle(HttpExchange ex) throws IOException {
				Map<String, String> q = query(ex.getRequestURI().getRawQuery());
				String msg;
				boolean ok = false;
				if (q.containsKey("error")) {
					msg = "Spotify denied access: " + q.get("error");
				} else if (state == null || !state.equals(q.get("state"))) {
					msg = "State mismatch, please retry from the game.";
				} else {
					try {
						exchangeCode(q.get("code"));
						ok = true;
						msg = "CONFUSION3H Client is connected to Spotify. You can close this tab.";
					} catch (IOException e) {
						msg = "Token exchange failed: " + e.getMessage();
					}
				}
				byte[] body = ("<html><body style='font-family:sans-serif;background:#0B0A12;color:#F2F0FA;padding:40px'><h2>"
						+ (ok ? "Connected" : "Not connected") + "</h2><p>" + escape(msg) + "</p></body></html>").getBytes(StandardCharsets.UTF_8);
				ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
				ex.sendResponseHeaders(ok ? 200 : 400, body.length);
				OutputStream os = ex.getResponseBody();
				os.write(body);
				os.close();
				AuthCallback cb = authCallback;
				if (cb != null) {
					cb.done(ok, msg);
				}
				if (ok) {
					stopCallbackServerLater();
				}
			}
		});
		server.start();
	}

	private void stopCallbackServerLater() {
		final HttpServer s = server;
		server = null;
		Thread t = new Thread(new Runnable() {
			public void run() {
				s.stop(1);
			}
		}, "spotify-callback-stop");
		t.setDaemon(true);
		t.start();
	}

	public synchronized void cancelAuthorization() {
		if (server != null) {
			server.stop(0);
			server = null;
		}
	}

	void exchangeCode(String code) throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		form.put("grant_type", "authorization_code");
		form.put("code", code);
		form.put("redirect_uri", REDIRECT_URI);
		form.put("client_id", clientId);
		form.put("code_verifier", verifier);
		applyTokens(postForm(ACCOUNTS + "/api/token", form));
	}

	private void refresh() throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		form.put("grant_type", "refresh_token");
		form.put("refresh_token", refreshToken);
		form.put("client_id", clientId);
		applyTokens(postForm(ACCOUNTS + "/api/token", form));
	}

	private synchronized void applyTokens(JsonObject o) {
		accessToken = o.get("access_token").getAsString();
		if (o.has("refresh_token")) {
			refreshToken = o.get("refresh_token").getAsString(); // PKCE rotates refresh tokens
		}
		expiresAt = System.currentTimeMillis() + o.get("expires_in").getAsLong() * 1000 - 30000;
		save();
	}

	public synchronized void logout() {
		accessToken = refreshToken = null;
		expiresAt = 0;
		tokenFile.delete();
	}

	private synchronized String token() throws IOException {
		if (refreshToken == null) {
			throw new IOException("Not connected to Spotify");
		}
		if (accessToken == null || System.currentTimeMillis() > expiresAt) {
			refresh();
		}
		return accessToken;
	}

	// ---- Player API -------------------------------------------------------------

	/** GET /me/player. Returns NOTHING when no device is active (HTTP 204). */
	public NowPlaying fetchState() throws IOException {
		JsonObject o = api("GET", "/me/player", null);
		if (o == null || !o.has("item") || o.get("item").isJsonNull()) {
			return NowPlaying.NOTHING;
		}
		JsonObject item = o.getAsJsonObject("item");
		String artist = null;
		if (item.has("artists")) {
			JsonArray arr = item.getAsJsonArray("artists");
			StringBuilder b = new StringBuilder();
			for (JsonElement e : arr) {
				if (b.length() > 0) {
					b.append(", ");
				}
				b.append(e.getAsJsonObject().get("name").getAsString());
			}
			artist = b.toString();
		} else if (item.has("show")) {
			artist = item.getAsJsonObject("show").get("name").getAsString();
		}
		long progress = o.has("progress_ms") && !o.get("progress_ms").isJsonNull() ? o.get("progress_ms").getAsLong() : 0;
		return new NowPlaying(item.get("name").getAsString(), artist, progress,
				item.get("duration_ms").getAsLong(), o.get("is_playing").getAsBoolean(), System.currentTimeMillis());
	}

	public void play() throws IOException { api("PUT", "/me/player/play", ""); }
	public void pause() throws IOException { api("PUT", "/me/player/pause", ""); }
	public void next() throws IOException { api("POST", "/me/player/next", ""); }
	public void previous() throws IOException { api("POST", "/me/player/previous", ""); }

	public void setVolume(int percent) throws IOException {
		api("PUT", "/me/player/volume?volume_percent=" + Math.max(0, Math.min(100, percent)), "");
	}

	public void setShuffle(boolean on) throws IOException {
		api("PUT", "/me/player/shuffle?state=" + on, "");
	}

	private JsonObject api(String method, String path, String body) throws IOException {
		HttpURLConnection c = open(API + path, method);
		c.setRequestProperty("Authorization", "Bearer " + token());
		if (body != null) {
			c.setDoOutput(true);
			c.setRequestProperty("Content-Length", "0");
			c.getOutputStream().close();
		}
		int code = c.getResponseCode();
		if (code == 204 || code == 202) {
			return null;
		}
		String text = readAll(code < 400 ? c.getInputStream() : c.getErrorStream());
		if (code == 401) {
			synchronized (this) {
				accessToken = null; // force refresh next call
			}
		}
		if (code >= 400) {
			throw new IOException(describe(code, text));
		}
		return text.isEmpty() ? null : new JsonParser().parse(text).getAsJsonObject();
	}

	static String describe(int code, String text) {
		String reason = "";
		try {
			JsonObject e = new JsonParser().parse(text).getAsJsonObject();
			if (e.has("error") && e.get("error").isJsonObject()) {
				reason = e.getAsJsonObject("error").get("message").getAsString();
			} else if (e.has("error_description")) {
				reason = e.get("error_description").getAsString();
			} else if (e.has("error")) {
				reason = e.get("error").getAsString();
			}
		} catch (Exception ignored) {
		}
		switch (code) {
		case 403: return "Spotify refused (Premium is required for playback control)" + (reason.isEmpty() ? "" : ": " + reason);
		case 404: return "No active Spotify device. Start playing in a Spotify app first.";
		case 429: return "Rate limited by Spotify, slowing down";
		default: return "Spotify HTTP " + code + (reason.isEmpty() ? "" : ": " + reason);
		}
	}

	private static JsonObject postForm(String url, Map<String, String> form) throws IOException {
		StringBuilder b = new StringBuilder();
		for (Map.Entry<String, String> e : form.entrySet()) {
			if (b.length() > 0) {
				b.append('&');
			}
			b.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
		}
		byte[] data = b.toString().getBytes(StandardCharsets.UTF_8);
		HttpURLConnection c = open(url, "POST");
		c.setDoOutput(true);
		c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		OutputStream os = c.getOutputStream();
		os.write(data);
		os.close();
		int code = c.getResponseCode();
		String text = readAll(code < 400 ? c.getInputStream() : c.getErrorStream());
		if (code >= 400) {
			throw new IOException(describe(code, text));
		}
		return new JsonParser().parse(text).getAsJsonObject();
	}

	private static HttpURLConnection open(String url, String method) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setRequestMethod(method);
		c.setConnectTimeout(8000);
		c.setReadTimeout(8000);
		c.setRequestProperty("User-Agent", "CONFUSION3H");
		return c;
	}

	private static String readAll(InputStream in) throws IOException {
		if (in == null) {
			return "";
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int n;
			while ((n = in.read(buf)) != -1) {
				out.write(buf, 0, n);
			}
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		} finally {
			in.close();
		}
	}

	// ---- helpers -------------------------------------------------------------------

	static String challenge(String verifier) {
		try {
			byte[] d = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
			return base64Url(d);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	static String randomUrlSafe(int bytes) {
		byte[] b = new byte[bytes];
		new SecureRandom().nextBytes(b);
		return base64Url(b);
	}

	private static String base64Url(byte[] b) {
		// java.util.Base64 is Java 8
		return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
	}

	private static String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	static Map<String, String> query(String raw) {
		Map<String, String> m = new HashMap<String, String>();
		if (raw == null) {
			return m;
		}
		for (String kv : raw.split("&")) {
			int eq = kv.indexOf('=');
			try {
				if (eq > 0) {
					m.put(URLDecoder.decode(kv.substring(0, eq), "UTF-8"), URLDecoder.decode(kv.substring(eq + 1), "UTF-8"));
				}
			} catch (java.io.UnsupportedEncodingException ignored) {
			}
		}
		return m;
	}

	private static String escape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private synchronized void load() {
		if (!tokenFile.isFile()) {
			return;
		}
		Properties p = new Properties();
		try {
			InputStream in = new FileInputStream(tokenFile);
			try {
				p.load(in);
			} finally {
				in.close();
			}
			refreshToken = p.getProperty("refresh_token");
		} catch (IOException ignored) {
		}
	}

	private synchronized void save() {
		Properties p = new Properties();
		if (refreshToken != null) {
			p.setProperty("refresh_token", refreshToken);
		}
		try {
			File dir = tokenFile.getParentFile();
			if (dir != null) {
				dir.mkdirs();
			}
			OutputStream out = new FileOutputStream(tokenFile);
			try {
				p.store(out, "CONFUSION3H Spotify token (keep private; delete to disconnect)");
			} finally {
				out.close();
			}
			tokenFile.setReadable(false, false);
			tokenFile.setReadable(true, true);
			tokenFile.setWritable(false, false);
			tokenFile.setWritable(true, true);
		} catch (IOException ignored) {
		}
	}
}
