package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.util.Keys;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/** 1.8.9 ToggleSprintMod: tap sprint to toggle, hold for vanilla behaviour, optional HUD line. */
public final class ToggleSprintModule extends SimpleNvgHudModule {

	private enum State {
		WALK("Walking"), VANILLA("Vanilla"), HELD("Key Held"), TOGGLED("Toggled");

		final String name;

		State(String name) {
			this.name = name;
		}
	}

	private final BooleanSetting hud = bool("hud", "tt:HUD", true);
	private final BooleanSetting always = bool("always", "tt:ALWAYS", false);
	private long startTime;
	private boolean wasDown;
	private State state = State.WALK;

	public ToggleSprintModule() {
		super("toggle_sprint", "tt:TOGGLE_SPRINT", "tt:TOGGLE_SPRINT_DESCRIPTION", 0F, 0.42F);
	}

	@Override
	protected void onDisable() {
		state = State.WALK;
		mc.options.sprintKey.setPressed(false);
	}

	@Override
	protected boolean visible() {
		return hud.enabled();
	}

	@Override
	public void onTick() {
		if (mc.player == null) {
			return;
		}
		int code = KeyBindingHelper.getBoundKeyOf(mc.options.sprintKey).getCode();
		boolean down = mc.currentScreen == null && Keys.isDown(code);
		if (!always.enabled() && mc.currentScreen == null) {
			if (down) {
				if (!wasDown) {
					startTime = System.currentTimeMillis();
					state = state == State.TOGGLED ? State.HELD : State.TOGGLED;
				} else if (System.currentTimeMillis() - startTime > 250) {
					state = State.HELD;
				}
			} else if (state == State.HELD && mc.player.isSprinting()) {
				state = State.VANILLA;
			} else if ((state == State.VANILLA || state == State.HELD) && !mc.player.isSprinting()) {
				state = State.WALK;
			}
			wasDown = down;
		}
		boolean sprint = state == State.HELD || state == State.TOGGLED || always.enabled();
		if (mc.currentScreen == null) {
			mc.options.sprintKey.setPressed(sprint || down);
		}
	}

	@Override
	public String getText() {
		if (always.enabled()) {
			return "Sprinting (Always)";
		}
		if (state == State.WALK) {
			return "Walking";
		}
		return "Sprinting (" + state.name + ")";
	}
}
