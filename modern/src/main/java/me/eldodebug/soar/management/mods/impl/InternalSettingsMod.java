package me.eldodebug.soar.management.mods.impl;

/** The 1.20.1 build has no shader blur pass; the 1.8.9 default (blur off) is kept. */
public final class InternalSettingsMod {

	private InternalSettingsMod() {
	}

	public static boolean isBlurEnabled() {
		return false;
	}
}
