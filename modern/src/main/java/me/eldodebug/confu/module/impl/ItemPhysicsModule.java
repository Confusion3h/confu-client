package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

public final class ItemPhysicsModule extends Module {

	private static ItemPhysicsModule instance;

	private final NumberSetting speed = number("speed", "tt:SPEED", 1, 0.5, 4, 0.1);

	public ItemPhysicsModule() {
		super("item_physics", Category.RENDER, "tt:ITEM_PHYSICS", "tt:ITEM_PHYSICS_DESCRIPTION");
		instance = this;
	}

	public static ItemPhysicsModule get() {
		return instance;
	}

	public float speed() {
		return speed.getFloat();
	}

	@Override
	protected void onEnable() {
		Items2DModule items2d = ConfuClient.get().modules().get(Items2DModule.class);
		if (items2d.isEnabled()) {
			items2d.setEnabled(false);
		}
	}
}
