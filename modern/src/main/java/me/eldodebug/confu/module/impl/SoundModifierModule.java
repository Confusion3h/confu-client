package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 SoundModifierMod: master volume/pitch multipliers. */
public final class SoundModifierModule extends Module {

	private final NumberSetting volume = number("volume", "tt:VOLUME", 1, 0, 2, 0.05);
	private final NumberSetting pitch = number("pitch", "tt:NOTE", 1, 0.5, 2, 0.05);

	public SoundModifierModule() {
		super("sound_modifier", Category.OTHER, "tt:SOUND_MODIFIER", "tt:SOUND_MODIFIER_DESCRIPTION");
	}

	public float volumeMul() { return isEnabled() ? volume.getFloat() : 1F; }
	public float pitchMul() { return isEnabled() ? pitch.getFloat() : 1F; }
}
