package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.TextHudModule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockModule extends TextHudModule {
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	public ClockModule() {
		super("clock", "module.clock.name", "module.clock.desc", 0F, 0.34F);
	}

	@Override
	protected String text() {
		return LocalTime.now().format(FORMAT);
	}
}
