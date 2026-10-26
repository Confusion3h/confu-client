package me.eldodebug.confu.util;

import me.eldodebug.confu.lang.Lang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public final class Keys {

	public static final int NONE = -1;

	private Keys() {
	}

	public static boolean isDown(int code) {
		if (code == NONE) {
			return false;
		}
		long handle = MinecraftClient.getInstance().getWindow().getHandle();
		return InputUtil.isKeyPressed(handle, code);
	}

	public static String name(int code) {
		if (code == NONE) {
			return Lang.get("menu.module.none");
		}
		return InputUtil.fromKeyCode(code, 0).getLocalizedText().getString();
	}
}
