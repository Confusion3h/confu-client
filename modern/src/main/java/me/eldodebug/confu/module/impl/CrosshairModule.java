package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class CrosshairModule extends Module {

	private static final Identifier SHEET = new Identifier(ConfuClient.MOD_ID, "textures/gui/crosshair.png");
	private static final int CELL = 16;
	private static final int SHEET_SIZE = 256;

	private final BooleanSetting custom = bool("custom", "setting.crosshair.custom", false);
	private final NumberSetting style = number("style", "setting.crosshair.style", 1, 0, 8, 1);
	private final NumberSetting scale = number("scale", "setting.crosshair.scale", 1, 0.3, 2, 0.05);
	private final ComboSetting color = combo("color", "setting.crosshair.color", "white", "white", "green", "red", "cyan", "yellow", "pink", "purple");
	private final NumberSetting opacity = number("opacity", "setting.crosshair.opacity", 1, 0.2, 1, 0.05);
	private final BooleanSetting hideThirdPerson = bool("hide_third", "setting.crosshair.hide_third", false);

	public CrosshairModule() {
		super("crosshair", Category.RENDER, "module.crosshair.name", "module.crosshair.desc");
	}

	public boolean hidesVanillaCrosshair() {
		return isEnabled() && (custom.enabled() || (hideThirdPerson.enabled() && !firstPerson()));
	}

	private boolean firstPerson() {
		return mc.options.getPerspective().isFirstPerson();
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		if (!custom.enabled() || (hideThirdPerson.enabled() && !firstPerson())) {
			return;
		}
		int centerX = mc.getWindow().getScaledWidth() / 2;
		int centerY = mc.getWindow().getScaledHeight() / 2;
		drawSprite(context, centerX, centerY, scale.getFloat(), style.getInt());
	}

	/** Preview inside a rectangle, centred, scaled 2x so the style is readable in the menu. */
	public void drawPreview(DrawContext context, int x, int y, int width, int height) {
		Draw.roundRect(context, x, y, width, height, 6, Theme.CARD_HOVER);
		drawSprite(context, x + width / 2, y + height / 2, scale.getFloat() * 2F, style.getInt());
	}

	private void drawSprite(DrawContext context, int centerX, int centerY, float size, int index) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(centerX, centerY, 0);
		matrices.scale(size, size, 1F);
		int rgb = rgb();
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(((rgb >> 16) & 0xFF) / 255F, ((rgb >> 8) & 0xFF) / 255F, (rgb & 0xFF) / 255F, opacity.getFloat());
		context.drawTexture(SHEET, -CELL / 2, -CELL / 2, index * CELL, 0, CELL, CELL, SHEET_SIZE, SHEET_SIZE);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		matrices.pop();
	}

	private int rgb() {
		return switch (color.get()) {
			case "green" -> 0x6EE7A3;
			case "red" -> 0xFF4D4D;
			case "cyan" -> 0x4DE1FF;
			case "yellow" -> 0xFFE14D;
			case "pink" -> 0xFF7AD9;
			case "purple" -> 0xA77BFF;
			default -> 0xFFFFFF;
		};
	}
}
