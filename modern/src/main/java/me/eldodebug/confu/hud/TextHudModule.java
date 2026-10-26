package me.eldodebug.confu.hud;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import net.minecraft.client.gui.DrawContext;

/** One line of text on a rounded Confu background, like the 1.8.9 SimpleHUDMod. */
public abstract class TextHudModule extends HudModule {

	private static final int PAD = 6;

	private final BooleanSetting background = bool("background", "setting.hud.background", true);
	private final NumberSetting scale = number("scale", "setting.hud.scale", 1, 0.5, 2, 0.05);

	protected TextHudModule(String id, String nameKey, String descriptionKey, float x, float y) {
		super(id, Category.HUD, nameKey, descriptionKey, x, y);
	}

	protected abstract String text();

	@Override
	protected int width() {
		return mc.textRenderer.getWidth(text()) + PAD * 2;
	}

	@Override
	protected int height() {
		return 8 + PAD * 2;
	}

	@Override
	protected float scale() {
		return scale.getFloat();
	}

	@Override
	protected void draw(DrawContext context, float tickDelta) {
		String text = text();
		if (background.enabled()) {
			Draw.roundRect(context, 0, 0, mc.textRenderer.getWidth(text) + PAD * 2, height(), 5, Theme.KEY);
		}
		context.drawText(mc.textRenderer, text, PAD, PAD, Theme.TEXT, !background.enabled());
	}
}
