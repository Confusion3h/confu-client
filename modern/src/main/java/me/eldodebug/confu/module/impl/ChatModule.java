package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ChatMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ChatModule extends Module {
	public ChatModule() {
		super("chat", Category.OTHER, "tt:CHAT", "tt:CHAT_DESCRIPTION");
	}
}
