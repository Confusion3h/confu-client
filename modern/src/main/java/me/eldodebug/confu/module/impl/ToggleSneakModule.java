package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.util.Keys;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/** 1.8.9 ToggleSneakMod. */
public final class ToggleSneakModule extends Module {

	private boolean toggle;
	private boolean wasDown;

	public ToggleSneakModule() {
		super("toggle_sneak", Category.PLAYER, "tt:TOGGLE_SNEAK", "tt:TOGGLE_SNEAK_DESCRIPTION");
	}

	@Override
	protected void onDisable() {
		toggle = false;
		wasDown = false;
		mc.options.sneakKey.setPressed(false);
	}

	@Override
	public void onTick() {
		if (mc.player == null) {
			return;
		}
		int code = KeyBindingHelper.getBoundKeyOf(mc.options.sneakKey).getCode();
		boolean down = mc.currentScreen == null && Keys.isDown(code);
		if (down && !wasDown) {
			toggle = !toggle;
		}
		wasDown = down;
		if (mc.currentScreen != null) {
			mc.options.sneakKey.setPressed(false);
		} else {
			mc.options.sneakKey.setPressed(toggle);
		}
	}
}
