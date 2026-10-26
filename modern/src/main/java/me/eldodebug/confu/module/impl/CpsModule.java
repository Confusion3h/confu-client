package me.eldodebug.confu.module.impl;

import java.util.ArrayDeque;
import java.util.Deque;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 CPSDisplayMod. */
public final class CpsModule extends SimpleNvgHudModule {

	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();
	private final BooleanSetting rightClick = bool("right", "tt:RIGHT_CLICK", true);
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public CpsModule() {
		super("cps", "tt:CPS_DISPLAY", "tt:CPS_DISPLAY_DESCRIPTION", 0F, 0.10F);
	}

	/** Called from the mouse mixin for every button press. */
	public static synchronized void click(int button) {
		long now = System.currentTimeMillis();
		if (button == 0) {
			LEFT.addLast(now);
		} else if (button == 1) {
			RIGHT.addLast(now);
		}
	}

	private static synchronized int count(Deque<Long> clicks) {
		long cutoff = System.currentTimeMillis() - 1000L;
		while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) {
			clicks.pollFirst();
		}
		return clicks.size();
	}

	@Override
	public String getText() {
		return (rightClick.enabled() ? count(LEFT) + " | " + count(RIGHT) : String.valueOf(count(LEFT))) + " CPS";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.MOUSE_POINTER : null;
	}
}
