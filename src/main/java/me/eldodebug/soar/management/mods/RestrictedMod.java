package me.eldodebug.soar.management.mods;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.utils.ServerUtils;

/**
 * Turns off mods that specific servers forbid. The list ships inside the jar
 * (assets/minecraft/soar/server-blacklist.json, a snapshot of upstream's list), so
 * nothing is downloaded.
 */
public class RestrictedMod {

	private static final String RESOURCE = "/assets/minecraft/soar/server-blacklist.json";

	String currentServerIP = "";
	public Boolean shouldCheck = true;
	private final Map<String, List<String>> blacklist = load();

	private static Map<String, List<String>> load() {
		Map<String, List<String>> out = new HashMap<String, List<String>>();
		InputStream in = RestrictedMod.class.getResourceAsStream(RESOURCE);
		if (in == null) {
			return out;
		}
		try {
			JsonObject root = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (JsonElement e : root.getAsJsonArray("blacklist")) {
				JsonObject o = e.getAsJsonObject();
				List<String> mods = new ArrayList<String>();
				for (JsonElement m : o.getAsJsonArray("mods")) {
					mods.add(m.getAsString());
				}
				out.put(o.get("serverip").getAsString(), Collections.unmodifiableList(mods));
			}
		} catch (Exception ex) {
			GlideLogger.error("Bad server blacklist", ex);
		} finally {
			try {
				in.close();
			} catch (Exception ignored) {
			}
		}
		return out;
	}

	public boolean checkAllowed(Mod m) {
		if (shouldCheck) {
			for (Map.Entry<String, List<String>> e : blacklist.entrySet()) {
				if (currentServerIP.contains(e.getKey()) && e.getValue().contains(m.getNameKey())) {
					m.setAllowed(false);
					return false;
				}
			}
		}
		m.setAllowed(true);
		return true;
	}

	public void joinServer(String ip) {
		// list is local; nothing to refresh
	}

	public void joinWorld() {
		this.currentServerIP = ServerUtils.getServerIP();
		for (Mod m : Glide.getInstance().getModManager().getMods()) {
			if (!checkAllowed(m) && m.isToggled()) {
				m.setToggled(false);
				Glide.getInstance().getNotificationManager().post(m.getName(), "Disabled: not allowed on this server", NotificationType.INFO);
			}
		}
	}
}
