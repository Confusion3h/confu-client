package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 TimeChangerMod: client-side time of day override. */
public final class TimeChangerModule extends Module {

	private final NumberSetting time = number("time", "tt:TIME", 12, 0, 24, 0.5);

	public TimeChangerModule() {
		super("time_changer", Category.WORLD, "tt:TIME_CHANGER", "tt:TIME_CHANGER_DESCRIPTION");
	}

	/** World time-of-day ticks (0–24000) when enabled, or -1 to leave vanilla. */
	public long overrideTimeOfDay() {
		if (!isEnabled()) {
			return -1L;
		}
		return (long) ((time.getFloat() / 24F) * 24000F) % 24000L;
	}
}
