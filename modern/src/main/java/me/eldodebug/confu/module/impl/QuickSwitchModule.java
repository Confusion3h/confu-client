package me.eldodebug.confu.module.impl;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.KeybindSetting;
import net.minecraft.item.SwordItem;

/** 1.8.9 QuickSwitchMod: keybind swaps to sword slot. */
public final class QuickSwitchModule extends Module {

	private final KeybindSetting bind = keybind(GLFW.GLFW_KEY_R);
	private int previous = -1;

	public QuickSwitchModule() {
		super("quick_switch", Category.PLAYER, "tt:QUICK_SWITCH", "tt:QUICK_SWITCH_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (mc.player == null || !keyPressed()) {
			return;
		}
		int sword = -1;
		for (int i = 0; i < 9; i++) {
			if (mc.player.getInventory().getStack(i).getItem() instanceof SwordItem) {
				sword = i;
				break;
			}
		}
		if (sword < 0) {
			return;
		}
		int cur = mc.player.getInventory().selectedSlot;
		if (cur == sword && previous >= 0) {
			mc.player.getInventory().selectedSlot = previous;
			previous = -1;
		} else {
			previous = cur;
			mc.player.getInventory().selectedSlot = sword;
		}
	}
}
