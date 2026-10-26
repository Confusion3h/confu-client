package me.eldodebug.soar.management.mods;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import me.eldodebug.confu.hud.HudModule;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.ModuleManager;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.management.mods.settings.Setting;

/** 1.8.9 ModManager API over the 1.20.1 ModuleManager: same mods, same settings objects underneath. */
public class ModManager {

	private final Map<Module, Mod> mods = new LinkedHashMap<>();
	private final Map<Mod, ArrayList<Setting>> settings = new LinkedHashMap<>();

	public ModManager(ModuleManager modules) {
		for (Module m : modules.all()) {
			Mod mod = m instanceof HudModule ? new HUDMod((HudModule) m) : new Mod(m);
			mods.put(m, mod);
			ArrayList<Setting> list = new ArrayList<>();
			for (me.eldodebug.confu.module.setting.Setting<?> s : m.settings()) {
				if (m instanceof me.eldodebug.confu.hud.NvgHudModule && "scale".equals(s.key())) {
					continue; // 1.8.9 scales HUD mods with the wheel in the HUD editor, not a setting
				}
				if (s instanceof BooleanSetting) {
					list.add(new me.eldodebug.soar.management.mods.settings.impl.BooleanSetting(mod, (BooleanSetting) s));
				} else if (s instanceof NumberSetting) {
					list.add(new me.eldodebug.soar.management.mods.settings.impl.NumberSetting(mod, (NumberSetting) s));
				} else if (s instanceof ComboSetting) {
					list.add(new me.eldodebug.soar.management.mods.settings.impl.ComboSetting(mod, (ComboSetting) s));
				} else if (s instanceof KeybindSetting) {
					list.add(new me.eldodebug.soar.management.mods.settings.impl.KeybindSetting(mod, (KeybindSetting) s));
				}
			}
			if (!list.isEmpty()) {
				settings.put(mod, list);
			}
		}
	}

	public ArrayList<Mod> getMods() {
		return new ArrayList<>(mods.values());
	}

	public Mod getMod(Module module) {
		return mods.get(module);
	}

	public ArrayList<HUDMod> getHudMods() {
		ArrayList<HUDMod> out = new ArrayList<>();
		for (Mod m : mods.values()) {
			if (m instanceof HUDMod) {
				out.add((HUDMod) m);
			}
		}
		return out;
	}

	public ArrayList<Setting> getSettingsByMod(Mod m) {
		return settings.get(m);
	}

	public String getWords(Mod m) {
		return m.getName() + " " + m.getDescription() + " " + m.getModule().id();
	}
}
