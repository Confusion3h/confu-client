package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;

/** 1.8.9 NameProtectMod: replace own username in rendered text. */
public final class NameProtectModule extends Module {

	private final ComboSetting name = combo("name", "tt:NAME", "You", "You", "Player", "Hidden");

	public NameProtectModule() {
		super("name_protect", Category.PLAYER, "tt:NAME_PROTECT", "tt:NAME_PROTECT_DESCRIPTION");
	}

	public String protect(String text) {
		if (!isEnabled() || mc.getSession() == null || text == null) {
			return text;
		}
		String user = mc.getSession().getUsername();
		if (user == null || user.isEmpty() || !text.contains(user)) {
			return text;
		}
		return text.replace(user, name.get());
	}
}
