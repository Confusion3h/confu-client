package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.StringSetting;
import me.eldodebug.confu.util.Keys;

/** 1.8.9 AutoTextMod: keybinds send chat lines (text stored in config JSON). */
public final class AutoTextModule extends Module {

	private final KeybindSetting key1 = extraKey("text1_key", "tt:TEXT_1_KEY", Keys.NONE);
	private final StringSetting text1 = string("text1", "tt:TEXT_1", "");
	private final KeybindSetting key2 = extraKey("text2_key", "tt:TEXT_2_KEY", Keys.NONE);
	private final StringSetting text2 = string("text2", "tt:TEXT_2", "");
	private final KeybindSetting key3 = extraKey("text3_key", "tt:TEXT_3_KEY", Keys.NONE);
	private final StringSetting text3 = string("text3", "tt:TEXT_3", "");
	private boolean was1, was2, was3;

	public AutoTextModule() {
		super("auto_text", Category.PLAYER, "tt:AUTO_TEXT", "tt:AUTO_TEXT_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.currentScreen != null) {
			was1 = was2 = was3 = false;
			return;
		}
		was1 = fire(key1, text1.get(), was1);
		was2 = fire(key2, text2.get(), was2);
		was3 = fire(key3, text3.get(), was3);
	}

	private boolean fire(KeybindSetting key, String text, boolean was) {
		boolean down = key.bound() && Keys.isDown(key.code());
		if (down && !was && text != null && !text.isEmpty()) {
			mc.player.networkHandler.sendChatMessage(text);
		}
		return down;
	}
}
