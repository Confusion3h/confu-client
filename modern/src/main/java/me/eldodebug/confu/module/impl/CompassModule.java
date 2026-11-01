package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 CompassMod (simple direction + fancy strip). */
public final class CompassModule extends SimpleNvgHudModule {

	private final ComboSetting design = combo("design", "tt:DESIGN", "simple", "simple", "fancy");
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final NumberSetting widthSetting = number("width", "tt:WIDTH", 180, 50, 450, 1);

	public CompassModule() {
		super("compass", "tt:COMPASS", "tt:COMPASS_DESCRIPTION", 0.30F, 0.34F);
	}

	@Override
	protected boolean visible() {
		return mc.player != null;
	}

	@Override
	protected void drawNanoVG() {
		if (mc.player == null || design.get().equals("simple")) {
			drawSimple();
			return;
		}
		int width = widthSetting.getInt();
		int angle = (int) MathHelper.wrapDegrees(mc.player.getYaw()) * -1 - 360;
		this.drawBackground(width, 29);
		this.drawRect((width / 2F) - 2F, 2.5F, 4, 4, getFontColor());
		for (int i = 0; i <= 2; i++) {
			int a = angle + i * 360;
			drawCardinal(width, a, "S", 0);
			drawCardinal(width, a, "W", 90);
			drawCardinal(width, a, "N", 180);
			drawCardinal(width, a, "E", 270);
		}
		this.setWidth(width);
		this.setHeight(29);
	}

	private void drawCardinal(int width, int angle, String s, int offset) {
		float x = (width / 2F) + angle + offset - 1.5F;
		this.drawRect((width / 2F) + angle + offset - 2F, 8, 0.8F, 9, getFontColor());
		this.drawCenteredText(s, x, 19, 8.5F, getHudFont(2));
	}

	@Override
	public String getText() {
		if (mc.player == null) {
			return "Direction: N";
		}
		String s = "Direction: ";
		double rotation = (mc.player.getHeadYaw() - 90) % 360;
		if (rotation < 0) {
			rotation += 360.0;
		}
		if (0 <= rotation && rotation < 22.5) {
			return s + "W";
		} else if (22.5 <= rotation && rotation < 67.5) {
			return s + "NW";
		} else if (67.5 <= rotation && rotation < 112.5) {
			return s + "N";
		} else if (112.5 <= rotation && rotation < 157.5) {
			return s + "NE";
		} else if (157.5 <= rotation && rotation < 202.5) {
			return s + "E";
		} else if (202.5 <= rotation && rotation < 247.5) {
			return s + "SE";
		} else if (247.5 <= rotation && rotation < 292.5) {
			return s + "S";
		} else if (292.5 <= rotation && rotation < 337.5) {
			return s + "SW";
		} else if (337.5 <= rotation && rotation < 360.0) {
			return s + "W";
		}
		return s + "Error";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.COMPASS : null;
	}
}
