package me.eldodebug.confu.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.eldodebug.confu.ConfuClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfuConfig {

	private static final String LANGUAGE = "language";
	private static final String MODULES = "modules";

	private final Path file;
	private JsonObject root;

	private ConfuConfig(Path file, JsonObject root) {
		this.file = file;
		this.root = root;
	}

	public static ConfuConfig load(Path file) {
		JsonObject root = new JsonObject();
		if (Files.isRegularFile(file)) {
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				JsonElement parsed = JsonParser.parseReader(reader);
				if (parsed.isJsonObject()) {
					root = parsed.getAsJsonObject();
				}
			} catch (IOException | RuntimeException e) {
				ConfuClient.LOGGER.warn("Could not read {}, starting with defaults", file, e);
			}
		}
		return new ConfuConfig(file, root);
	}

	public String language(String fallback) {
		return root.has(LANGUAGE) && root.get(LANGUAGE).isJsonPrimitive()
				? root.get(LANGUAGE).getAsString()
				: fallback;
	}

	public void setLanguage(String code) {
		root.addProperty(LANGUAGE, code);
	}

	public JsonObject modules() {
		if (!root.has(MODULES) || !root.get(MODULES).isJsonObject()) {
			root.add(MODULES, new JsonObject());
		}
		return root.getAsJsonObject(MODULES);
	}

	public void save() {
		try {
			Files.createDirectories(file.getParent());
			Path temp = file.resolveSibling(file.getFileName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
			}
			Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			ConfuClient.LOGGER.warn("Could not save {}", file, e);
		}
	}
}
