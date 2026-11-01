package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
/** 1.8.9 PackDisplayMod (name of active pack; icon polish later). */
public final class PackDisplayModule extends NvgHudModule {

	private final BooleanSetting compact = bool("compact", "tt:COMPACT", false);

	public PackDisplayModule() {
		super("pack_display", Category.HUD, "tt:PACK_DISPLAY", "tt:PACK_DISPLAY_DESCRIPTION", 0.5F, 0.30F);
	}

	@Override
	protected void drawNanoVG() {
		String name = "Default";
		if (mc.getResourcePackManager() != null) {
			for (var profile : mc.getResourcePackManager().getEnabledProfiles()) {
				if (!"vanilla".equals(profile.getName()) && !"fabric".equals(profile.getName())) {
					name = profile.getDisplayName().getString();
				}
			}
		}
		boolean compactMode = compact.enabled();
		float stringWidth = this.getTextWidth(name, 9, getHudFont(1));
		float textX = compactMode ? 8F : 8F;
		float textY = compactMode ? 5.5F : 7F;
		this.drawBackground((compactMode ? 16 : 16) + stringWidth, compactMode ? 18 : 24);
		this.drawText(name, textX, textY, 9, getHudFont(1));
		this.setWidth((int) ((compactMode ? 16 : 16) + stringWidth));
		this.setHeight(compactMode ? 18 : 24);
	}
}
