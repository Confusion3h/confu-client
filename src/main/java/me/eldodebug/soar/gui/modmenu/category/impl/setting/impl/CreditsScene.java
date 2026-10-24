package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** Settings > Credits: CONFUSION3H and its GPL-3.0 upstreams. */
public class CreditsScene extends SettingScene {

	private static final String[][] CREDITS = {
		{ "CONFUSION3H", "This client: rebrand, FPS mode, launcher, logo" },
		{ "Glide Client", "Upstream fork \u00b7 github.com/GlideClient/client" },
		{ "Soar Client by EldoDebug", "The original client Glide and CONFUSION3H build on" },
	};

	public CreditsScene(SettingCategory parent) {
		super(parent, TranslateText.CREDITS, TranslateText.CREDITS_DESCRIPTION, LegacyIcon.INFO);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
		float x = getX(), y = getY(), w = getWidth();

		nvg.drawRoundedRect(x, y, w, 58, 6, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawLogo(x + 10, y + 10, 38, palette.getFontColor(ColorType.DARK));
		nvg.drawText("CONFUSION3H", x + 56, y + 16, palette.getFontColor(ColorType.DARK), 15, Fonts.SEMIBOLD);
		nvg.drawText("v" + Glide.getInstance().getVersion() + " \u00b7 GPL-3.0 \u00b7 Minecraft 1.8.9", x + 56, y + 34, palette.getFontColor(ColorType.NORMAL), 8, Fonts.REGULAR);

		float oy = y + 68;
		for (String[] c : CREDITS) {
			nvg.drawRoundedRect(x, oy, w, 36, 6, palette.getBackgroundColor(ColorType.DARK));
			nvg.drawText(c[0], x + 10, oy + 8, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
			nvg.drawText(c[1], x + 10, oy + 22, palette.getFontColor(ColorType.NORMAL), 7.5F, Fonts.REGULAR);
			oy += 44;
		}
		nvg.drawText("Licensed under GPL-3.0; upstream copyright notices are kept in the source.", x, oy + 4,
				new Color(palette.getFontColor(ColorType.NORMAL).getRGB() & 0xFFFFFF | 0xB0000000, true), 7, Fonts.REGULAR);
	}
}
