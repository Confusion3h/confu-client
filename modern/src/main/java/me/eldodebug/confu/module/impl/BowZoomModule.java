package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.item.Items;

/** 1.8.9 BowZoomMod. */
public final class BowZoomModule extends Module {

	private final NumberSetting factor = number("factor", "tt:FACTOR", 5, 1, 15, 1);

	public BowZoomModule() {
		super("bow_zoom", Category.PLAYER, "tt:BOW_ZOOM", "tt:BOW_ZOOM_DESCRIPTION");
	}

	public float fovMultiplier() {
		if (!isEnabled() || mc.player == null) {
			return 1F;
		}
		if (!(mc.player.isUsingItem() && mc.player.getActiveItem().isOf(Items.BOW))) {
			return 1F;
		}
		int duration = Math.min(mc.player.getItemUseTime(), 20);
		float modifier = duration / 20F;
		modifier = (modifier * modifier + modifier * 2F) / 3F;
		if (modifier > 1F) {
			modifier = 1F;
		}
		return 1F - modifier * 0.15F * factor.getFloat();
	}
}
