package me.eldodebug.confu.module.impl;

import java.util.ArrayList;
import java.util.List;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;

/** 1.8.9 SoundSubtitlesMod: recent sound names HUD. */
public final class SoundSubtitlesModule extends NvgHudModule {

	private final List<String> lines = new ArrayList<>();

	public SoundSubtitlesModule() {
		super("sound_subtitles", Category.HUD, "tt:SOUND_SUBTITLES", "tt:SOUND_SUBTITLES_DESCRIPTION", 0.02F, 0.80F);
	}

	public void push(String name) {
		if (!isEnabled()) return;
		lines.add(0, name);
		while (lines.size() > 6) lines.remove(lines.size() - 1);
	}

	@Override
	protected void drawNanoVG() {
		List<String> show = lines;
		if (show.isEmpty() && isEditing()) {
			show = List.of("Block.break", "Entity.hurt");
		}
		float max = 40;
		for (String s : show) max = Math.max(max, getTextWidth(s, 8, getHudFont(1)));
		this.drawBackground(max + 10, Math.max(18, show.size() * 12 + 6));
		float y = 5;
		for (String s : show) {
			this.drawText(s, 5, y, 8, getHudFont(1));
			y += 12;
		}
		this.setWidth((int) (max + 10));
		this.setHeight((int) Math.max(18, show.size() * 12 + 6));
	}
}
