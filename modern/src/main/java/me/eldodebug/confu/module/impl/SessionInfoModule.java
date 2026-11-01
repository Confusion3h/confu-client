package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 SessionInfoMod (session time + server + kill count). */
public final class SessionInfoModule extends NvgHudModule {

	private long startTime = System.currentTimeMillis();
	private int killCount;

	public SessionInfoModule() {
		super("session_info", Category.HUD, "tt:SESSION_INFO", "tt:SESSION_INFO_DESCRIPTION", 0F, 0.70F);
	}

	@Override
	protected void onEnable() {
		startTime = System.currentTimeMillis();
	}

	public void onJoinedServer() {
		startTime = System.currentTimeMillis();
		killCount = 0;
	}

	public void onKill() {
		killCount++;
	}

	@Override
	protected void drawNanoVG() {
		String time;
		if (mc.isInSingleplayer()) {
			time = "Singleplayer";
		} else {
			long durationInMillis = System.currentTimeMillis() - startTime;
			long second = (durationInMillis / 1000) % 60;
			long minute = (durationInMillis / (1000 * 60)) % 60;
			long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
			time = String.format("%02d:%02d:%02d", hour, minute, second);
		}
		String ip = "Single Player";
		if (!mc.isInSingleplayer() && mc.getCurrentServerEntry() != null) {
			ip = mc.getCurrentServerEntry().address;
		}
		this.drawBackground(140, 64);
		this.drawText("Session Info", 5.5F, 6F, 10.5F, getHudFont(1));
		this.drawRect(0, 17.5F, 140, 1, getFontColor(120));
		this.drawText(LegacyIcon.CLOCK, 5.5F, 22.5F, 10F, Fonts.LEGACYICON);
		this.drawText(time, 18, 24, 9, getHudFont(1));
		this.drawText(LegacyIcon.SERVER, 5.5F, 22.5F + 13, 10F, Fonts.LEGACYICON);
		this.drawText(ip, 18, 24 + 12, 9, getHudFont(1));
		this.drawText(LegacyIcon.USER, 5.5F, 22.5F + 26, 10F, Fonts.LEGACYICON);
		this.drawText(killCount + " kill", 18, 24 + 26.5F, 9, getHudFont(1));
		this.setWidth(140);
		this.setHeight(64);
	}
}
