package me.eldodebug.soar.management.profile;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.Theme;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.Language;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.file.FileUtils;

public class ProfileManager {

	private CopyOnWriteArrayList<Profile> profiles = new CopyOnWriteArrayList<Profile>();
	
	public ProfileManager() {
		
		
		this.loadProfiles(false); // confu-modern.json stays the live config on 1.20.1
	}
	
	public void loadProfiles(boolean loadDefaultProfile) {

		try {
			File profileDir = Glide.getInstance().getFileManager().getProfileDir();
			int id = 0;

			profiles.clear();

			for (File f : profileDir.listFiles()) {

				if (FileUtils.getExtension(f).equals("json")) {

					if (f.getName().equals("Default.json")) {
						if (loadDefaultProfile) {
							load(f);
						}
					} else {
						try (FileReader reader = new FileReader(f)) {

							String serverIp = "";
							ProfileIcon icon = ProfileIcon.GRASS;
							ProfileType type = ProfileType.ALL;

							Gson gson = new Gson();
							JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
							JsonObject profileData = JsonUtils.getObjectProperty(jsonObject, "Profile Data");

							serverIp = JsonUtils.getStringProperty(profileData, "Server", "");
							icon = ProfileIcon.getIconById(JsonUtils.getIntProperty(profileData, "Icon", ProfileIcon.GRASS.getId()));
							type = ProfileType.getTypeById(JsonUtils.getIntProperty(profileData, "Type", ProfileType.ALL.getId()));

							Profile p = new Profile(id, serverIp, f, icon);

							p.setType(type);

							profiles.add(p);

							id++;
						} catch (Exception e) {
							GlideLogger.error("Failed to load profile", e);
						}
					}
				}
			}
		} catch (Exception ignored) {}
		profiles.add(new Profile(999, "", null, null));
	}
	
	/** 1.20.1: a profile is the "Profile Data" header plus a snapshot of every Confu module (same JSON as confu-modern.json). */
	public void save(File file, String serverIp, ProfileType type, ProfileIcon icon) {
		try (FileWriter writer = new FileWriter(file)) {
			JsonObject jsonObject = new JsonObject();
			JsonObject profileData = new JsonObject();
			profileData.addProperty("Icon", icon.getId());
			profileData.addProperty("Type", type.getId());
			profileData.addProperty("Server", serverIp);
			jsonObject.add("Profile Data", profileData);
			JsonObject appJsonObject = new JsonObject();
			appJsonObject.addProperty("Accent Color", Glide.getInstance().getColorManager().getCurrentColor().getName());
			appJsonObject.addProperty("Theme", Glide.getInstance().getColorManager().getTheme().getId());
			jsonObject.add("Appearance", appJsonObject);
			JsonObject modules = new JsonObject();
			for (me.eldodebug.confu.module.Module m : me.eldodebug.confu.ConfuClient.get().modules().all()) {
				JsonObject mj = new JsonObject();
				m.write(mj);
				modules.add(m.id(), mj);
			}
			jsonObject.add("Modules", modules);
			new Gson().toJson(jsonObject, writer);
		} catch (Exception e) {
			GlideLogger.error("Failed to save profile", e);
		}
	}

	public void save() {
		save(new File(Glide.getInstance().getFileManager().getProfileDir(), "Default.json"), "", ProfileType.ALL, ProfileIcon.GRASS);
	}

	public void load(File file) {
		if (file == null) {
			return;
		}
		try (FileReader reader = new FileReader(file)) {
			JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
			JsonObject modules = JsonUtils.getObjectProperty(jsonObject, "Modules");
			if (modules != null) {
				for (me.eldodebug.confu.module.Module m : me.eldodebug.confu.ConfuClient.get().modules().all()) {
					if (modules.has(m.id()) && modules.get(m.id()).isJsonObject()) {
						m.read(modules.getAsJsonObject(m.id()));
					}
				}
			}
			me.eldodebug.confu.ConfuClient.get().saveConfig();
		} catch (Exception e) {
			GlideLogger.error("Failed to load profile", e);
		}
	}

	public void delete(Profile profile) {
		profiles.remove(profile);
		profile.getJsonFile().delete();
	}
	
	public CopyOnWriteArrayList<Profile> getProfiles() {
		return profiles;
	}
}
