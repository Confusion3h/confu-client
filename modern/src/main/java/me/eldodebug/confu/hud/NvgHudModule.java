package me.eldodebug.confu.hud;

import java.awt.Color;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.GuiEditHUD;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * A HUD element drawn exactly like the 1.8.9 HUDMod: NanoVG, Inter fonts, the HUD theme
 * (Normal, Glow, Outline, Vanilla, Dark, Light, Rect, Modern, Text, ...) and the accent colour.
 * The drawing helpers below are the 1.8.9 HUDMod ones, offset by the element's position and scale.
 */
public abstract class NvgHudModule extends HudModule {

	private final NumberSetting scale;
	private int width = 100, height = 18;
	private float x, y, s = 1F;

	protected NvgHudModule(String id, Category category, String nameKey, String descriptionKey, float defaultX, float defaultY) {
		super(id, category, nameKey, descriptionKey, defaultX, defaultY);
		this.scale = number("scale", "tt:SCALE", 1, 0.5, 2, 0.05);
	}

	/** 1.8.9 drawNanoVG(): draw with the helpers, then call setWidth/setHeight. */
	protected abstract void drawNanoVG();

	/** Vanilla-renderer layer for things NanoVG cannot draw (item icons); runs after drawNanoVG. */
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
	}

	@Override
	protected int width() {
		return width;
	}

	@Override
	protected int height() {
		return height;
	}

	@Override
	protected float scale() {
		return scale.getFloat();
	}

	@Override
	protected void draw(DrawContext context, float tickDelta) {
	}

	protected boolean visible() {
		return true;
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		if (!visible() && !isEditing()) {
			return;
		}
		int sw = mc.getWindow().getScaledWidth();
		int sh = mc.getWindow().getScaledHeight();
		int px = Math.round(x() * Math.max(0F, sw - scaledWidth()));
		int py = Math.round(y() * Math.max(0F, sh - scaledHeight()));
		context.draw();
		NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
		nvg.setupAndDraw(() -> {
			this.x = px;
			this.y = py;
			this.s = scale();
			drawNanoVG();
		});
		drawVanilla(context, px, py, scale());
	}

	public boolean isEditing() {
		return mc.currentScreen instanceof GuiEditHUD;
	}

	protected void setWidth(int w) {
		this.width = w;
	}

	protected void setHeight(int h) {
		this.height = h;
	}

	protected float getX() {
		return x;
	}

	protected float getY() {
		return y;
	}

	protected float getScale() {
		return s;
	}

	protected NanoVGManager nvg() {
		return Glide.getInstance().getNanoVGManager();
	}

	// ---- 1.8.9 HUDMod helpers ---------------------------------------------------------

	public static String theme() {
		ConfuClient c = ConfuClient.get();
		return c == null ? "normal" : c.hudTheme().get();
	}

	public void save() {
		nvg().save();
	}

	public void restore() {
		nvg().restore();
	}

	public void scale(float addX, float addY, float width, float height, float nvgScale) {
		nvg().scale(x + (addX * s), y + (addY * s), width * s, height * s, nvgScale);
	}

	public void drawRect(float addX, float addY, float width, float height, Color color) {
		nvg().drawRect(x + (addX * s), y + (addY * s), width * s, height * s, color);
	}

	public void drawRoundedRect(float addX, float addY, float width, float height, float radius, Color color) {
		if (width < 0 || height < 0) {
			return;
		}
		nvg().drawRoundedRect(x + (addX * s), y + (addY * s), width * s, height * s, radius * s, color);
	}

	public void drawRoundedRect(float addX, float addY, float width, float height, float radius) {
		drawRoundedRect(addX, addY, width, height, radius, getFontColor());
	}

	public void drawBackground(float addX, float addY, float width, float height, float radius) {
		NanoVGManager nvg = nvg();
		AccentColor currentColor = Glide.getInstance().getColorManager().getCurrentColor();
		String t = theme();
		boolean isNormal = t.equals("normal"), isVanilla = t.equals("vanilla"), isGlow = t.equals("glow"),
				isVanillaGlow = t.equals("vanilla_glow"), isOutline = t.equals("outline"), isOutlineGlow = t.equals("outline_glow"),
				isShadow = t.equals("shadow"), isDark = t.equals("dark"), isLight = t.equals("light"), isRect = t.equals("rect"),
				isModern = t.equals("modern"), isSimpGrad = t.equals("gradient_simple");
		float lastWidth = width * s;
		float lastHeight = height * s;
		float x = this.x + (addX * s);
		float y = this.y + (addY * s);
		if (isNormal || isVanilla || isShadow || isDark || isLight || isModern) {
			nvg.drawShadow(x, y, lastWidth, lastHeight, radius - 0.75F);
		} else if (isGlow || isVanillaGlow) {
			nvg.drawGradientShadow(x, y, lastWidth, lastHeight, radius, currentColor.getColor1(), currentColor.getColor2());
		} else if (isOutline) {
			nvg.drawShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2);
		} else if (isOutlineGlow) {
			nvg.drawGradientShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2, currentColor.getColor1(), currentColor.getColor2());
		}
		if (isOutline || isOutlineGlow) {
			nvg.drawGradientOutlineRoundedRect(x - 1, y - 1, lastWidth + 2, lastHeight + 2, radius + 1, 1.5F, currentColor.getColor1(), currentColor.getColor2());
		}
		if (isVanilla || isVanillaGlow || isOutline || isOutlineGlow) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 100));
		} else if (isNormal || isGlow) {
			nvg.drawGradientRoundedRect(x, y, lastWidth, lastHeight, radius, ColorUtils.applyAlpha(currentColor.getColor1(), 220), ColorUtils.applyAlpha(currentColor.getColor2(), 220));
		} else if (isLight) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(240, 240, 240, 220));
		} else if (isDark) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(20, 20, 20, 220));
		}
		if (isRect || isSimpGrad) {
			nvg.drawRect(x, y, lastWidth, lastHeight, new Color(20, 20, 20, 165));
		}
		if (isSimpGrad) {
			nvg.drawHorizontalGradientRect(x, y - (2 * s), lastWidth, (2 * s), ColorUtils.interpolateColors(8, 0, currentColor.getColor1(), currentColor.getColor2()), ColorUtils.interpolateColors(10, 20, currentColor.getColor1(), currentColor.getColor2()));
		}
		if (isModern) {
			nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 110));
			nvg.drawOutlineRoundedRect(x - 0.5F, y - 0.5F, lastWidth + 1, lastHeight + 1, radius + 0.5F, 0.7F, new Color(255, 255, 255, 110));
		}
	}

	public void drawBackground(float width, float height) {
		drawBackground(0, 0, width, height, 6 * s);
	}

	public void drawBackground(float addX, float addY, float width, float height) {
		drawBackground(addX, addY, width, height, 6 * s);
	}

	public void drawBackground(float width, float height, float radius) {
		drawBackground(0, 0, width, height, radius);
	}

	public void drawText(String text, float addX, float addY, float size, Font font, Color color) {
		NanoVGManager nvg = nvg();
		float lastSize = size * s;
		if (theme().equals("text")) {
			nvg.save();
			nvg.fontBlur(20);
			nvg.drawText(text, x + (addX * s), y + (addY * s), new Color(0, 0, 0, 150), lastSize, font);
			nvg.restore();
		}
		nvg.drawText(text, x + (addX * s), y + (addY * s), new Color(color.getRed(), color.getGreen(), color.getBlue(), 180), lastSize, font);
	}

	public void drawText(String text, float addX, float addY, float size, Font font) {
		drawText(text, addX, addY, size, font, getFontColor());
	}

	public void drawCenteredText(String text, float addX, float addY, float size, Font font, Color color) {
		nvg().drawCenteredText(text, x + (addX * s), y + (addY * s), color, size * s, font);
	}

	public void drawCenteredText(String text, float addX, float addY, float size, Font font) {
		drawCenteredText(text, addX, addY, size, font, getFontColor());
	}

	public float getTextWidth(String text, float size, Font font) {
		return nvg().getTextWidth(text, size, font);
	}

	public Color getFontColor(int alpha) {
		String t = theme();
		if (t.equals("dark") || t.equals("light")) {
			return Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor(alpha);
		}
		return new Color(255, 255, 255, alpha);
	}

	public Color getFontColor() {
		return getFontColor(255);
	}

	public Font getHudFont(int in) {
		switch (in) {
		case 2:
			return Fonts.MEDIUM;
		case 3:
			return Fonts.SEMIBOLD;
		default:
			return Fonts.REGULAR;
		}
	}
}
