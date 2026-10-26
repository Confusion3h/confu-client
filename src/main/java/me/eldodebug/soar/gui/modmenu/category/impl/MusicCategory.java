package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.GuiAddTrack;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.MusicPlayerMod;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.music.NowPlaying;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;

/**
 * Music player panel. Takes the sidebar slot the minigames used to have and is
 * only shown while the Music Player mod is enabled.
 */
public class MusicCategory extends Category {

	private static final TranslateText[] BUTTONS = { TranslateText.MUSIC_PREV, TranslateText.MUSIC_PLAYPAUSE, TranslateText.MUSIC_NEXT,
			TranslateText.MUSIC_ADDLINK, TranslateText.MUSIC_OPENFOLDER, TranslateText.MUSIC_RESCAN };

	public MusicCategory(GuiModMenu parent) {
		super(parent, TranslateText.MUSIC_PLAYER, LegacyIcon.MUSIC, false, true);
	}

	public static boolean available() {
		MusicPlayerMod mod = MusicPlayerMod.getInstance();
		return mod != null && mod.isToggled();
	}

	private MusicManager music() {
		return Glide.getInstance().getMusicManager();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		ColorPalette palette = Glide.getInstance().getColorManager().getPalette();
		float x = this.getX() + 15, y = this.getY() + 15, w = this.getWidth() - 30;

		NowPlaying np = music().getNowPlaying();
		nvg.drawRoundedRect(x, y, w, 56, 8, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawText(LegacyIcon.MUSIC, x + 12, y + 18, palette.getFontColor(ColorType.DARK), 20, Fonts.LEGACYICON);
		String title = np.isEmpty() ? TranslateText.MUSIC_NOTHING.getText() : np.title;
		String sub = np.isEmpty() ? TranslateText.MUSIC_HINT.getText() : (np.artist != null ? np.artist : (np.playing ? TranslateText.MUSIC_PLAYING.getText() : TranslateText.MUSIC_PAUSED.getText()));
		nvg.drawText(title == null ? "" : title, x + 44, y + 14, palette.getFontColor(ColorType.DARK), 11, Fonts.SEMIBOLD);
		nvg.drawText(sub, x + 44, y + 30, palette.getFontColor(ColorType.NORMAL), 8, Fonts.REGULAR);
		if (np.durationMs > 0) {
			float frac = Math.min(1F, np.positionNow() / (float) np.durationMs);
			nvg.drawRoundedRect(x + 44, y + 44, w - 56, 3, 1.5F, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawRoundedRect(x + 44, y + 44, (w - 56) * frac, 3, 1.5F, Glide.getInstance().getColorManager().getCurrentColor().getColor1());
		}

		float bw = (w - 10 * (BUTTONS.length / 2 - 1)) / (BUTTONS.length / 2);
		for (int i = 0; i < BUTTONS.length; i++) {
			float bx = x + (i % 3) * (bw + 10), by = y + 68 + (i / 3) * 32;
			boolean hover = MouseUtils.isInside(mouseX, mouseY, bx, by, bw, 24);
			if (hover) {
				AccentColor accent = Glide.getInstance().getColorManager().getCurrentColor();
				nvg.drawGradientRoundedRect(bx, by, bw, 24, 6, accent.getColor1(), accent.getColor2());
			} else {
				nvg.drawRoundedRect(bx, by, bw, 24, 6, palette.getBackgroundColor(ColorType.DARK));
			}
			nvg.drawCenteredText(BUTTONS[i].getText(), bx + bw / 2, by + 8, hover ? Color.WHITE : palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0) {
			return;
		}
		float x = this.getX() + 15, y = this.getY() + 15, w = this.getWidth() - 30;
		float bw = (w - 10 * (BUTTONS.length / 2 - 1)) / (BUTTONS.length / 2);
		for (int i = 0; i < BUTTONS.length; i++) {
			float bx = x + (i % 3) * (bw + 10), by = y + 68 + (i / 3) * 32;
			if (!MouseUtils.isInside(mouseX, mouseY, bx, by, bw, 24)) {
				continue;
			}
			switch (i) {
			case 0: music().previous(); break;
			case 1: music().togglePause(); break;
			case 2: music().next(); break;
			case 3: mc.displayGuiScreen(new GuiAddTrack(mc.currentScreen)); break;
			case 4: FileUtils.openFolderAtPath(music().getMusicDir()); break;
			case 5: music().rescan(); break;
			}
		}
	}
}
