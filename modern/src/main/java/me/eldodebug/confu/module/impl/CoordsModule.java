package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

/** 1.8.9 CoordsMod: Simple (one line) or Fancy (X, Y, Z and biome). */
public final class CoordsModule extends SimpleNvgHudModule {

	private final ComboSetting design = combo("design", "tt:DESIGN", "simple", "simple", "fancy");
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public CoordsModule() {
		super("coords", "tt:COORDS", "tt:COORDS_DEDSCRIPTION", 0F, 0.26F);
	}

	@Override
	protected boolean visible() {
		return mc.player != null;
	}

	@Override
	protected void drawNanoVG() {
		if (mc.player == null) {
			drawSimple();
			return;
		}
		if (design.get().equals("simple")) {
			drawSimple();
			return;
		}
		BlockPos pos = mc.player.getBlockPos();
		String biome = mc.world.getBiome(pos).getKey()
				.map(k -> Text.translatable(Util.createTranslationKey("biome", k.getValue())).getString()).orElse("");
		int maxWidth;
		float biomeWidth = this.getTextWidth("Biome: " + biome, 9, getHudFont(1));
		if (100 < biomeWidth) {
			maxWidth = (int) (biomeWidth + 12);
		} else {
			maxWidth = 107;
		}
		this.drawBackground(maxWidth, 48);
		this.drawText("X: " + (int) mc.player.getX(), 5.5F, 5.5F, 9, getHudFont(1));
		this.drawText("Y: " + (int) mc.player.getY(), 5.5F, 15.5F, 9, getHudFont(1));
		this.drawText("Z: " + (int) mc.player.getZ(), 5.5F, 25.5F, 9, getHudFont(1));
		this.drawText("Biome: " + biome, 5.5F, 35.5F, 9, getHudFont(1));
		this.setWidth(maxWidth);
		this.setHeight(48);
	}

	@Override
	public String getText() {
		if (mc.player == null) {
			return "X: 0 Y: 0 Z: 0 ";
		}
		return "X: " + (int) mc.player.getX() + " Y: " + (int) mc.player.getY() + " Z: " + (int) mc.player.getZ() + " ";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.MAP_PIN : null;
	}
}
