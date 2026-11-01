package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ChatTranslateMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ChatTranslateModule extends Module {
	public ChatTranslateModule() {
		super("chat_translate", Category.OTHER, "tt:CHAT_TRANSLATE", "tt:CHAT_TRANSLATE_DESCRIPTION");
	}
}
