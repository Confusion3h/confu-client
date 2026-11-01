package me.eldodebug.confu.module.impl;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.KeybindSetting;

/** 1.8.9 HypixelQuickPlayMod: keybind opens /play menu command. */
public final class HypixelQuickPlayModule extends Module {

	private final KeybindSetting bind = keybind(GLFW.GLFW_KEY_H);

	public HypixelQuickPlayModule() {
		super("hypixel_quick_play", Category.PLAYER, "tt:HYPIXEL_QUICK_PLAY", "tt:HYPIXEL_QUICK_PLAY_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (mc.player != null && keyPressed()) {
			mc.player.networkHandler.sendChatCommand("play");
		}
	}
}
