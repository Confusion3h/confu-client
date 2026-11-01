package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** 1.8.9 InventoryDisplayMod: main inventory mirror. */
public final class InventoryDisplayModule extends NvgHudModule {

	public InventoryDisplayModule() {
		super("inventory_display", Category.HUD, "tt:INVENTORY_DISPLAY", "tt:INVENTORY_DISPLAY_DESCRIPTION", 0.5F, 0.55F);
	}

	@Override
	protected void drawNanoVG() {
		this.setWidth(18 * 9);
		this.setHeight(18 * 3);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		MatrixStack m = context.getMatrices();
		m.push();
		m.translate(px, py, 0);
		m.scale(scale, scale, 1F);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				ItemStack stack;
				if (isEditing() || mc.player == null) {
					stack = new ItemStack(Items.STONE);
				} else {
					stack = mc.player.getInventory().getStack(9 + row * 9 + col);
				}
				if (!stack.isEmpty()) {
					context.drawItem(stack, col * 18, row * 18);
					context.drawItemInSlot(mc.textRenderer, stack, col * 18, row * 18);
				}
			}
		}
		m.pop();
	}
}
