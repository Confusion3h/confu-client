package me.eldodebug.confu.module.impl;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import net.minecraft.client.option.Perspective;

/** 1.8.9 TaplookMod: hold key for front/behind third person. */
public final class TaplookModule extends Module {

	private final ComboSetting mode = combo("perspective", "tt:PERSPECTIVE", "front", "front", "behind");
	private final KeybindSetting bind = keybind(GLFW.GLFW_KEY_P);
	private boolean active;
	private Perspective prev;

	public TaplookModule() {
		super("taplook", Category.PLAYER, "tt:TAPLOOK", "tt:TAPLOOK_DESCRIPTION");
	}

	@Override
	protected void onDisable() {
		stop();
	}

	@Override
	public void onTick() {
		boolean down = keyDown();
		if (down && !active) {
			start();
		} else if (!down && active) {
			stop();
		}
	}

	private void start() {
		active = true;
		prev = mc.options.getPerspective();
		mc.options.setPerspective(mode.get().equals("front") ? Perspective.THIRD_PERSON_FRONT : Perspective.THIRD_PERSON_BACK);
	}

	private void stop() {
		if (!active) {
			return;
		}
		active = false;
		if (prev != null) {
			mc.options.setPerspective(prev);
		}
	}
}
