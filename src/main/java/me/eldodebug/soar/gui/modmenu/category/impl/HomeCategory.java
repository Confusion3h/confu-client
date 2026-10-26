package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.impl.FPSDisplayMod;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.mods.impl.MusicPlayerMod;
import me.eldodebug.soar.management.music.NowPlaying;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.mouse.MouseUtils;

/**
 * Local-only home: client info, quick toggles, tips, now playing. No network content
 * (the upstream news/changelog/Discord cards were fetched from Glide's servers).
 */
public class HomeCategory extends Category {

	private static final int PAD = 15, LEFT_W = 200, RIGHT_X = 230, RIGHT_W = 174;
	private static final TranslateText[] TIPS = {
			TranslateText.HOME_TIP1, TranslateText.HOME_TIP2, TranslateText.HOME_TIP3,
			TranslateText.HOME_TIP4, TranslateText.HOME_TIP5,
	};

	public HomeCategory(GuiModMenu parent) {
		super(parent, TranslateText.HOME, LegacyIcon.HOME, false, false);
	}

	private interface Toggle {
		String label();
		boolean on();
		void flip();
	}

	private final Toggle[] toggles = {
			new Toggle() {
				public String label() { return TranslateText.HOME_FPSMODE.getText(); }
				public boolean on() { return InternalSettingsMod.isFpsMode(); }
				public void flip() { InternalSettingsMod.getInstance().getFpsModeSetting().setToggled(!on()); }
			},
			modToggle(TranslateText.HOME_FPSDISPLAY, FPSDisplayMod.class),
			modToggle(TranslateText.HOME_MUSICPLAYER, MusicPlayerMod.class),
	};

	private static Toggle modToggle(final TranslateText label, final Class<? extends Mod> type) {
		return new Toggle() {
			Mod find() {
				for (Mod m : Glide.getInstance().getModManager().getMods()) {
					if (type.isInstance(m)) {
						return m;
					}
				}
				return null;
			}
			public String label() { return label.getText(); }
			public boolean on() { Mod m = find(); return m != null && m.isToggled(); }
			public void flip() { Mod m = find(); if (m != null) m.toggle(); }
		};
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorPalette palette = instance.getColorManager().getPalette();
		AccentColor accent = instance.getColorManager().getCurrentColor();
		Color card = palette.getBackgroundColor(ColorType.DARK);
		Color text = palette.getFontColor(ColorType.DARK);
		Color muted = palette.getFontColor(ColorType.NORMAL);
		float x = getX(), y = getY();

		// left: identity + quick toggles
		nvg.drawRoundedRect(x + PAD, y + PAD, LEFT_W, 250, 8, card);
		nvg.drawLogo(x + PAD + 10, y + PAD + 12, 34, accent.getInterpolateColor());
		nvg.drawText("Confu Client", x + PAD + 52, y + PAD + 16, text, 11, Fonts.SEMIBOLD);
		nvg.drawText("v" + instance.getVersion() + " \u00b7 Minecraft 1.8.9", x + PAD + 52, y + PAD + 33, muted, 8, Fonts.REGULAR);
		nvg.drawText(TranslateText.HOME_QUICKTOGGLES.getText(), x + PAD + 10, y + PAD + 66, text, 10, Fonts.SEMIBOLD);
		for (int i = 0; i < toggles.length; i++) {
			float ry = y + PAD + 84 + i * 30;
			nvg.drawText(toggles[i].label(), x + PAD + 10, ry + 5, text, 9, Fonts.MEDIUM);
			boolean on = toggles[i].on();
			float sx = x + PAD + LEFT_W - 44;
			nvg.drawRoundedRect(sx, ry, 32, 17, 8.5F, on ? accent.getInterpolateColor() : palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawRoundedRect(on ? sx + 17 : sx + 2, ry + 2, 13, 13, 6.5F, Color.WHITE);
		}
		nvg.drawTextBox(TranslateText.HOME_BASEDON.getText(),
				x + PAD + 10, y + PAD + 210, LEFT_W - 20, muted, 7.5F, Fonts.REGULAR);

		// right top: tips
		nvg.drawRoundedRect(x + RIGHT_X, y + PAD, RIGHT_W, 151, 8, card);
		nvg.drawText(TranslateText.HOME_TIPS.getText(), x + RIGHT_X + 8, y + PAD + 8, text, 11, Fonts.SEMIBOLD);
		float ty = y + PAD + 28;
		for (TranslateText tipText : TIPS) {
			String tip = tipText.getText();
			nvg.drawRoundedRect(x + RIGHT_X + 9, ty + 3, 4, 4, 2, accent.getInterpolateColor());
			nvg.drawTextBox(tip, x + RIGHT_X + 18, ty, RIGHT_W - 26, text, 7.5F, Fonts.REGULAR);
			ty += 23;
		}

		// right bottom: now playing
		float by = y + PAD + 164;
		nvg.drawRoundedRect(x + RIGHT_X, by, RIGHT_W, 86, 8, card);
		nvg.drawText(TranslateText.HOME_NOWPLAYING.getText(), x + RIGHT_X + 8, by + 8, text, 11, Fonts.SEMIBOLD);
		NowPlaying np = instance.getMusicManager().getNowPlaying();
		if (np.isEmpty()) {
			nvg.drawTextBox(TranslateText.HOME_NOTHINGYET.getText(), x + RIGHT_X + 8, by + 30, RIGHT_W - 16, muted, 8, Fonts.REGULAR);
		} else {
			nvg.drawTextBox(np.title, x + RIGHT_X + 8, by + 28, RIGHT_W - 16, text, 9, Fonts.MEDIUM);
			if (np.artist != null) {
				nvg.drawText(np.artist, x + RIGHT_X + 8, by + 50, muted, 8, Fonts.REGULAR);
			}
			if (np.durationMs > 0) {
				float frac = Math.min(1F, np.positionNow() / (float) np.durationMs);
				nvg.drawRoundedRect(x + RIGHT_X + 8, by + 70, RIGHT_W - 16, 3, 1.5F, palette.getBackgroundColor(ColorType.NORMAL));
				nvg.drawRoundedRect(x + RIGHT_X + 8, by + 70, (RIGHT_W - 16) * frac, 3, 1.5F, accent.getInterpolateColor());
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0) {
			return;
		}
		for (int i = 0; i < toggles.length; i++) {
			float ry = getY() + PAD + 84 + i * 30;
			if (MouseUtils.isInside(mouseX, mouseY, getX() + PAD + 6, ry - 4, LEFT_W - 12, 25)) {
				toggles[i].flip();
			}
		}
	}
}
