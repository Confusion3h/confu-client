package me.eldodebug.confu.hud;

import me.eldodebug.confu.module.Category;
import me.eldodebug.soar.management.nanovg.font.Fonts;

/** The 1.8.9 SimpleHUDMod: optional icon plus one line of text on the themed background. */
public abstract class SimpleNvgHudModule extends NvgHudModule {

	protected SimpleNvgHudModule(String id, String nameKey, String descriptionKey, float x, float y) {
		super(id, Category.HUD, nameKey, descriptionKey, x, y);
	}

	public String getText() {
		return null;
	}

	public String getIcon() {
		return null;
	}

	@Override
	protected void drawNanoVG() {
		drawSimple();
	}

	protected final void drawSimple() {
		String text = getText();
		if (text == null) {
			return;
		}
		boolean hasIcon = getIcon() != null;
		float addX = hasIcon ? this.getTextWidth(getIcon(), 9.5F, Fonts.LEGACYICON) + 4 : 0;
		float bgWidth = (this.getTextWidth(text, 9, getHudFont(1)) + 10) + addX;
		this.drawBackground(bgWidth, 18);
		this.drawText(text, 5.5F + addX, 5.5F, 9, getHudFont(1));
		if (hasIcon) {
			this.drawText(getIcon(), 5.5F, 4F, 10.4F, Fonts.LEGACYICON);
		}
		this.setWidth((int) bgWidth);
		this.setHeight(18);
	}
}
