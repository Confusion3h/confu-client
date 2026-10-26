package me.eldodebug.confu.lang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.eldodebug.confu.ConfuClient;
import net.minecraft.client.MinecraftClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Lang {

	public static final String ENGLISH = "en_us";
	public static final String ITALIAN = "it_it";

	private static final Map<String, Map<String, String>> TABLES = new HashMap<>();
	private static String current = ENGLISH;

	private Lang() {
	}

	public static String detectFromGame() {
		try {
			String code = MinecraftClient.getInstance().getLanguageManager().getLanguage();
			return ITALIAN.equals(code) ? ITALIAN : ENGLISH;
		} catch (RuntimeException e) {
			return ENGLISH;
		}
	}

	public static void setLanguage(String code) {
		current = ITALIAN.equals(code) ? ITALIAN : ENGLISH;
	}

	public static String current() {
		return current;
	}

	public static String get(String key) {
		if (key.startsWith("tt:")) {
			try {
				return me.eldodebug.soar.management.language.TranslateText.valueOf(key.substring(3)).getText();
			} catch (IllegalArgumentException e) {
				return key.substring(3);
			}
		}
		String value = table(current).get(key);
		if (value == null) {
			value = table(ENGLISH).get(key);
		}
		return value == null ? key : value;
	}

	public static String format(String key, Object... args) {
		return String.format(get(key), args);
	}

	private static Map<String, String> table(String code) {
		return TABLES.computeIfAbsent(code, Lang::load);
	}

	private static Map<String, String> load(String code) {
		Map<String, String> out = new HashMap<>();
		String path = "/assets/" + ConfuClient.MOD_ID + "/lang/" + code + ".json";
		try (InputStream in = Lang.class.getResourceAsStream(path)) {
			if (in == null) {
				ConfuClient.LOGGER.warn("Missing language file {}", path);
				return out;
			}
			JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
				out.put(entry.getKey(), entry.getValue().getAsString());
			}
		} catch (Exception e) {
			ConfuClient.LOGGER.warn("Could not read {}", path, e);
		}
		return out;
	}
}
