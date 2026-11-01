package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 DayCounterMod. */
public final class DayCounterModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public DayCounterModule() {
		super("day_counter", "tt:DAY_COUNTER", "tt:DAY_COUNTER_DESCRIPTION", 0.15F, 0.18F);
	}

	@Override
	protected boolean visible() {
		return mc.world != null;
	}

	@Override
	public String getText() {
		long time = mc.world == null ? 0 : mc.world.getTime() / 24000L;
		return time + " Day" + (time != 1L ? "s" : "");
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.SUNRISE : null;
	}
}
