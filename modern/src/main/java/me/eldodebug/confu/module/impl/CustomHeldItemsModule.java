package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

public final class CustomHeldItemsModule extends Module {

	private static CustomHeldItemsModule instance;

	private final NumberSetting x = number("x", "tt:X", 0.75, -1, 1, 0.01);
	private final NumberSetting y = number("y", "tt:Y", -0.15, -1, 1, 0.01);
	private final NumberSetting z = number("z", "tt:Z", -1, -1, 1, 0.01);

	private final NumberSetting xScale = number("x_scale", "tt:X_SCALE", 1, 0, 1, 0.01);
	private final NumberSetting yScale = number("y_scale", "tt:Y_SCALE", 1, 0, 1, 0.01);
	private final NumberSetting zScale = number("z_scale", "tt:Z_SCALE", 1, 0, 1, 0.01);

	public CustomHeldItemsModule() {
		super("custom_held_items", Category.RENDER, "tt:CUSTOM_HELD_ITEMS", "tt:CUSTOM_HELD_ITEMS_DESCRIPTION");
		instance = this;
	}

	public static CustomHeldItemsModule get() {
		return instance;
	}

	public float x() {
		return x.getFloat();
	}

	public float y() {
		return y.getFloat();
	}

	public float z() {
		return z.getFloat();
	}

	public float xScale() {
		return xScale.getFloat();
	}

	public float yScale() {
		return yScale.getFloat();
	}

	public float zScale() {
		return zScale.getFloat();
	}
}
