package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;

/** 1.8.9 ClientSpooferMod: spoof client brand. */
public final class ClientSpooferModule extends Module {

	private final ComboSetting type = combo("type", "tt:TYPE", "vanilla", "vanilla", "forge");

	public ClientSpooferModule() {
		super("client_spoofer", Category.OTHER, "tt:CLIENT_SPOOFER", "tt:CLIENT_SPOOFER_DESCRIPTION");
	}

	public String brand() {
		if (!isEnabled()) {
			return null;
		}
		return type.get().equals("forge") ? "fml,forge" : "vanilla";
	}
}
