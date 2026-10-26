package me.eldodebug.soar.management.mods.settings;

import me.eldodebug.confu.lang.Lang;
import me.eldodebug.soar.management.mods.Mod;

/** 1.8.9 setting API over a 1.20.1 module setting. */
public class Setting {

	private final Mod parent;
	private final me.eldodebug.confu.module.setting.Setting<?> raw;

	public Setting(Mod parent, me.eldodebug.confu.module.setting.Setting<?> raw) {
		this.parent = parent;
		this.raw = raw;
	}

	public void reset() {
		if (raw != null) {
			raw.reset();
			changed();
		}
	}

	protected void changed() {
		me.eldodebug.confu.ConfuClient.get().saveConfig();
	}

	public Mod getParent() {
		return parent;
	}

	public String getName() {
		return raw == null ? "" : Lang.get(raw.nameKey());
	}

	public String getNameKey() {
		return raw == null ? "" : raw.nameKey();
	}
}
