package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.notification.NotificationType;

public final class Items2DModule extends Module {

	private static Items2DModule instance;

	public Items2DModule() {
		super("items_2d", Category.RENDER, "tt:ITEMS_2D", "tt:ITEMS_2D_DESCRIPTION");
		instance = this;
	}

	public static Items2DModule get() {
		return instance;
	}

	@Override
	protected void onEnable() {
		ItemPhysicsModule physics = ConfuClient.get().modules().get(ItemPhysicsModule.class);
		if (physics.isEnabled()) {
			physics.setEnabled(false);
			Glide.getInstance().getNotificationManager().post(physics.name(), "Disabled due to incompatibility", NotificationType.WARNING);
		}
	}
}
