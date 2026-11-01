package me.eldodebug.confu.module.impl;

import java.text.DecimalFormat;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.util.Keys;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.TimerUtils;

/** 1.8.9 StopwatchMod. */
public final class StopwatchModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final KeybindSetting bind = extraKey("stopwatch_key", "tt:KEYBIND", GLFW.GLFW_KEY_P);
	private final TimerUtils timer = new TimerUtils();
	private final DecimalFormat timeFormat = new DecimalFormat("0.00");
	private int pressCount;
	private float currentTime;
	private boolean wasDown;

	public StopwatchModule() {
		super("stopwatch", "tt:STOPWATCH", "tt:STOPWATCH_DESCRIPTION", 0.30F, 0.26F);
	}

	@Override
	protected void onEnable() {
		timer.reset();
		pressCount = 0;
		currentTime = 0;
		wasDown = false;
	}

	@Override
	public void onTick() {
		boolean down = mc.currentScreen == null && bind.bound() && Keys.isDown(bind.code());
		if (down && !wasDown) {
			pressCount++;
		}
		wasDown = down;
		switch (pressCount) {
		case 0:
			timer.reset();
			break;
		case 1:
			currentTime = timer.getElapsedTime() / 1000F;
			break;
		case 3:
			timer.reset();
			currentTime = 0;
			pressCount = 0;
			break;
		default:
			break;
		}
	}

	@Override
	public String getText() {
		return timeFormat.format(currentTime) + " s";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.WATCH : null;
	}
}
