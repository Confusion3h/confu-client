package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/** 1.8.9 WeatherDisplayMod. */
public final class WeatherDisplayModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public WeatherDisplayModule() {
		super("weather_display", "tt:WEATHER_DISPLAY", "tt:WEATHER_DISPLAY_DESCRIPTION", 0.30F, 0.10F);
	}

	@Override
	protected boolean visible() {
		return mc.world != null && mc.player != null;
	}

	@Override
	public String getText() {
		return "Weather: " + weatherName();
	}

	@Override
	public String getIcon() {
		if (!icon.enabled()) {
			return null;
		}
		String w = weatherName();
		if ("Snowing".equals(w)) {
			return LegacyIcon.CLOUD_SNOW;
		}
		if ("Raining".equals(w)) {
			return LegacyIcon.CLOUD_RAIN;
		}
		if ("Thundering".equals(w)) {
			return LegacyIcon.CLOUD_LIGHTING;
		}
		return LegacyIcon.SUN;
	}

	private String weatherName() {
		if (mc.world == null || mc.player == null) {
			return "Cleaning";
		}
		boolean snowy = false;
		Biome biome = mc.world.getBiome(mc.player.getBlockPos()).value();
		Identifier id = mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
		if (id != null && (id.getPath().contains("hill") || id.getPath().contains("peak") || id.getPath().contains("snow"))) {
			snowy = mc.player.getY() > 100 || id.getPath().contains("snow");
		}
		if (mc.world.isRaining()) {
			return snowy ? "Snowing" : "Raining";
		}
		if (mc.world.isThundering()) {
			return "Thundering";
		}
		return "Cleaning";
	}
}
