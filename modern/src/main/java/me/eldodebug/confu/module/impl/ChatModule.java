package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 ChatMod: smooth chat, player heads, infinite history, background and compact duplicates. */
public final class ChatModule extends Module {

	private final BooleanSetting smooth = bool("smooth", "tt:SMOOTH", true);
	private final NumberSetting smoothSpeed = number("smooth_speed", "tt:SMOOTH_SPEED", 4, 1, 10, 1);
	private final BooleanSetting head = bool("head", "tt:HEAD", false);
	private final BooleanSetting infinity = bool("infinity", "tt:INFINITY", false);
	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final BooleanSetting compact = bool("compact", "tt:COMPACT", false);

	public ChatModule() {
		super("chat", Category.OTHER, "tt:CHAT", "tt:CHAT_DESCRIPTION");
	}

	public boolean smooth() {
		return isEnabled() && smooth.enabled();
	}

	public float smoothSpeed() {
		return smoothSpeed.getFloat();
	}

	public boolean head() {
		return isEnabled() && head.enabled();
	}

	public boolean infinity() {
		return isEnabled() && infinity.enabled();
	}

	public boolean background() {
		return !isEnabled() || background.enabled();
	}

	public boolean compact() {
		return isEnabled() && compact.enabled();
	}
}
