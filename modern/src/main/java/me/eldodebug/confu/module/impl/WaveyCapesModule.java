package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 WaveyCapesMod — stick-simulated capes, same settings and defaults. */
public final class WaveyCapesModule extends Module {

	private static WaveyCapesModule instance;

	private final NumberSetting gravity = number("gravity", "tt:GRAVITY", 15, 2, 30, 1);
	private final ComboSetting movement = combo("movement", "tt:MOVEMENT", "basic", "vanilla", "basic");
	private final ComboSetting style = combo("style", "tt:STYLE", "smooth", "blocky", "smooth");
	private final ComboSetting mode = combo("mode", "tt:MODE", "waves", "none", "waves");
	private final NumberSetting heightMultiplier = number("height_multiplier", "tt:HEIGHT_MULTIPLIER", 6, 2, 10, 1);

	public WaveyCapesModule() {
		super("wavey_capes", Category.RENDER, "tt:WAVEY_CAPES", "tt:WAVEY_CAPES_DESCRIPTION");
		instance = this;
	}

	public static WaveyCapesModule get() {
		return instance;
	}

	@Override
	protected void onEnable() {
		MoBendsModule mobends = ConfuClient.get().modules().get(MoBendsModule.class);
		if (mobends != null && mobends.isEnabled()) {
			mobends.setEnabled(false);
		}
	}

	public float gravity() {
		return gravity.getFloat();
	}

	public boolean basicMovement() {
		return "basic".equals(movement.get());
	}

	public boolean smoothStyle() {
		return "smooth".equals(style.get());
	}

	public boolean waves() {
		return "waves".equals(mode.get());
	}

	public int heightMultiplier() {
		return heightMultiplier.getInt();
	}
}
