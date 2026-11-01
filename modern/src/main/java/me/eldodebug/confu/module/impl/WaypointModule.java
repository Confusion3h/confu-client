package me.eldodebug.confu.module.impl;

import java.util.ArrayList;
import java.util.List;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.KeybindSetting;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

/** 1.8.9 WaypointMod: basic save-current-pos keybind (full GUI later). */
public final class WaypointModule extends Module {

	private final KeybindSetting add = keybind(GLFW.GLFW_KEY_B);
	private final List<BlockPos> points = new ArrayList<>();

	public WaypointModule() {
		super("waypoint", Category.WORLD, "tt:WAYPOINT", "tt:WAYPOINT_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (mc.player != null && keyPressed()) {
			points.add(mc.player.getBlockPos());
		}
	}

	public List<BlockPos> points() { return points; }
}
