package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.TextHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsModule extends TextHudModule {

	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();

	private final BooleanSetting showRight = bool("right", "setting.cps.right", true);

	public CpsModule() {
		super("cps", "module.cps.name", "module.cps.desc", 0F, 0.10F);
	}

	/** Called from the mouse mixin for every button press, in game or not. */
	public static void click(int button) {
		long now = System.currentTimeMillis();
		if (button == 0) {
			LEFT.addLast(now);
		} else if (button == 1) {
			RIGHT.addLast(now);
		}
	}

	private static int count(Deque<Long> clicks) {
		long cutoff = System.currentTimeMillis() - 1000L;
		while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) {
			clicks.pollFirst();
		}
		return clicks.size();
	}

	@Override
	protected String text() {
		return showRight.enabled() ? count(LEFT) + " | " + count(RIGHT) + " CPS" : count(LEFT) + " CPS";
	}
}
