package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 WeatherChangerMod. */
public final class WeatherChangerModule extends Module {

	private final ComboSetting weather = combo("weather", "tt:WEATHER", "clear", "clear", "rain", "storm", "snow");
	private final NumberSetting rainStrength = number("rain_strength", "tt:RAIN_STRENGTH", 1, 0, 1, 0.05);
	private final NumberSetting thunderStrength = number("thunder_strength", "tt:THUNDER_STRENGTH", 1, 0, 1, 0.05);

	public WeatherChangerModule() {
		super("weather_changer", Category.WORLD, "tt:WEATHER_CHANGER", "tt:WEATHER_CHANGER_DESCRIPTION");
	}

	public float rain(float vanilla) {
		if (!isEnabled()) {
			return vanilla;
		}
		String w = weather.get();
		if (w.equals("clear")) {
			return 0F;
		}
		if (w.equals("snow") || w.equals("rain") || w.equals("storm")) {
			return rainStrength.getFloat();
		}
		return vanilla;
	}

	public float thunder(float vanilla) {
		if (!isEnabled()) {
			return vanilla;
		}
		return weather.get().equals("storm") ? thunderStrength.getFloat() : 0F;
	}
}
