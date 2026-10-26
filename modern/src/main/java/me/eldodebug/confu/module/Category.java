package me.eldodebug.confu.module;

import me.eldodebug.confu.lang.Lang;

public enum Category {
	ALL("category.all"),
	PLAYER("category.player"),
	RENDER("category.render"),
	HUD("category.hud"),
	WORLD("category.world"),
	OTHER("category.other");

	private final String langKey;

	Category(String langKey) {
		this.langKey = langKey;
	}

	public String displayName() {
		return Lang.get(langKey);
	}
}
