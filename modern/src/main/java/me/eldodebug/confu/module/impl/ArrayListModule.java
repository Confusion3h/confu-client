package me.eldodebug.confu.module.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;

/** 1.8.9 ArrayListMod. */
public final class ArrayListModule extends NvgHudModule {

	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final BooleanSetting hud = bool("hud", "tt:HUD", false);
	private final BooleanSetting render = bool("render", "tt:RENDER", false);
	private final BooleanSetting player = bool("player", "tt:PLAYER", false);
	private final BooleanSetting other = bool("other", "tt:OTHER", false);
	private final ComboSetting mode = combo("mode", "tt:MODE", "right", "right", "left");

	public ArrayListModule() {
		super("array_list", Category.HUD, "tt:ARRAY_LIST", "tt:ARRAY_LIST_DESCRIPTION", 1F, 0F);
	}

	@Override
	protected void drawNanoVG() {
		AccentColor currentColor = Glide.getInstance().getColorManager().getCurrentColor();
		List<Module> enabledMods = new ArrayList<>();
		int maxWidth = 0;
		for (Module m : ConfuClient.get().modules().all()) {
			if (m == this || !m.isEnabled()) {
				continue;
			}
			if (!hud.enabled() && m.category() == Category.HUD) {
				continue;
			}
			if (!render.enabled() && m.category() == Category.RENDER) {
				continue;
			}
			if (!player.enabled() && m.category() == Category.PLAYER) {
				continue;
			}
			if (!other.enabled() && (m.category() == Category.OTHER || m.category() == Category.WORLD)) {
				continue;
			}
			float nameWidth = this.getTextWidth(m.name(), 8.5F, getHudFont(1));
			enabledMods.add(m);
			if (maxWidth < nameWidth) {
				maxWidth = (int) nameWidth;
			}
		}
		enabledMods.sort((a, b) -> Float.compare(
				this.getTextWidth(b.name(), 8.5F, getHudFont(1)),
				this.getTextWidth(a.name(), 8.5F, getHudFont(1))));
		int y = 0;
		int colorIndex = 0;
		boolean isRight = mode.get().equals("right");
		for (Module m : enabledMods) {
			float nameWidth = this.getTextWidth(m.name(), 8.5F, getHudFont(1));
			if (background.enabled()) {
				this.drawRect(isRight ? (maxWidth - nameWidth) : 0, y, nameWidth + 5, 12, new Color(0, 0, 0, 100));
			}
			this.drawText(m.name(), 3 + (isRight ? (maxWidth - nameWidth) : 0), y + 2.5F, 8.5F, getHudFont(1),
					currentColor.getInterpolateColor(colorIndex));
			y += 12;
			colorIndex -= 10;
		}
		this.setWidth(maxWidth + 4);
		this.setHeight(Math.max(y, 12));
	}
}
