package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;

/** 1.8.9 ChatTranslateMod: click-to-translate icon and translated outgoing chat. */
public final class ChatTranslateModule extends Module {

	private static boolean suppressIcon;

	private final ComboSetting language = combo("language", "tt:LANGUAGE", "japanese", "japanese", "english", "chinese", "polish");

	public ChatTranslateModule() {
		super("chat_translate", Category.OTHER, "tt:CHAT_TRANSLATE", "tt:CHAT_TRANSLATE_DESCRIPTION");
	}

	public String language() {
		return language.get();
	}

	/** 1.8.9's GuiChat "Translate: On/Off" state, toggled in the chat screen. */
	private boolean outgoing;
	private String target = "ja";

	public boolean outgoing() {
		return isEnabled() && outgoing;
	}

	public void toggleOutgoing() {
		outgoing = !outgoing;
	}

	public String targetLanguage() {
		return target;
	}

	public void cycleTargetLanguage() {
		switch (target) {
			case "ja" -> target = "en";
			case "en" -> target = "zh-Hant";
			case "zh-Hant" -> target = "zh-Hans";
			default -> target = "ja";
		}
	}

	public static boolean iconSuppressed() {
		return suppressIcon;
	}

	public static void setIconSuppressed(boolean value) {
		suppressIcon = value;
	}
}
