package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

/** 1.8.9 MemoryUsageMod. */
public final class MemoryUsageModule extends SimpleNvgHudModule {

	private final ComboSetting design = combo("design", "tt:DESIGN", "simple", "simple", "fancy");
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final SimpleAnimation animation = new SimpleAnimation();

	public MemoryUsageModule() {
		super("memory_usage", "tt:MEMORY_USAGE", "tt:MEMORY_USAGE_DESCRIPTION", 0.15F, 0.26F);
	}

	@Override
	protected void drawNanoVG() {
		if (design.get().equals("simple")) {
			drawSimple();
			return;
		}
		animation.setAnimation((getUsingMemory() / 100F) * 360, 16);
		this.drawBackground(54, 60);
		this.drawCenteredText("Memory", 54 / 2F, 6, 9, getHudFont(1));
		this.drawCenteredText(getUsingMemory() + "%", 54 / 2F, 32, 9, getHudFont(1));
		this.drawArc(27, 35.5F, 16.5F, -90, 360, 1.6F, this.getFontColor(120));
		this.drawArc(27, 35.5F, 16.5F, -90, animation.getValue() - 90, 1.6F, this.getFontColor());
		this.setWidth(54);
		this.setHeight(60);
	}

	@Override
	public String getText() {
		return "Mem: " + getUsingMemory() + "%";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.SERVER : null;
	}

	private long getUsingMemory() {
		Runtime runtime = Runtime.getRuntime();
		return (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory();
	}
}
