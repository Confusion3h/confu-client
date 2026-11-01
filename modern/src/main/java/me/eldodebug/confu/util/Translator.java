package me.eldodebug.confu.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.eldodebug.soar.utils.TimerUtils;

/** Same Microsoft Translator endpoint the 1.8.9 client uses for ChatTranslate. */
public final class Translator {

	private static String authCache;
	private static TimerUtils timer;

	public static final String AUTO_DETECT = "";
	public static final String ENGLISH = "en";
	public static final String JAPANESE = "ja";
	public static final String CHINESE_SIMPLIFIED = "zh-Hans";
	public static final String CHINESE_TRADITIONAL = "zh-Hant";
	public static final String POLISH = "pl";

	private Translator() {
	}

	private static String auth() throws Exception {
		if (timer == null) {
			timer = new TimerUtils();
		}
		if (timer.delay(300 * 1000) || authCache == null) {
			HttpURLConnection con = (HttpURLConnection) new URL("https://edge.microsoft.com/translate/auth").openConnection();
			con.setRequestMethod("GET");
			StringBuilder content = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = in.readLine()) != null) {
					content.append(line);
				}
			}
			authCache = content.toString();
			return authCache;
		}
		return authCache;
	}

	public static String translate(String text, String from, String to) throws Exception {
		HttpURLConnection con = (HttpURLConnection) new URL("https://api.cognitive.microsofttranslator.com/translate?from="
				+ from + "&to=" + to + "&api-version=3.0&includeSentenceLength=true").openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("authorization", "Bearer " + auth());
		con.setDoOutput(true);

		JsonObject body = new JsonObject();
		body.addProperty("Text", text);
		JsonArray bodyArray = new JsonArray();
		bodyArray.add(body);
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = bodyArray.toString().getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}

		StringBuilder responseContent = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				responseContent.append(line.trim());
			}
		}

		JsonArray jsonArray = JsonParser.parseString(responseContent.toString()).getAsJsonArray();
		StringJoiner result = new StringJoiner(",");
		for (JsonElement json : jsonArray) {
			JsonArray translations = json.getAsJsonObject().getAsJsonArray("translations");
			for (JsonElement trans : translations) {
				result.add(trans.getAsJsonObject().get("text").getAsString());
			}
		}
		return result.toString();
	}
}
