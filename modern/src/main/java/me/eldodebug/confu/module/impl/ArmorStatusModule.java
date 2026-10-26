package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** 1.8.9 ArmorStatusMod: the four armour icons, horizontal or vertical, with optional durability bar/count. */
public final class ArmorStatusModule extends NvgHudModule {

	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private final ComboSetting mode = combo("mode", "tt:MODE", "horizontal", "vertical", "horizontal");
	private final BooleanSetting value = bool("value", "tt:VALUE", false);

	public ArmorStatusModule() {
		super("armor_status", Category.HUD, "tt:ARMOR_STATUS", "tt:ARMOR_STATUS_DESCRIPTION", 1F, 0.5F);
	}

	private boolean vertical() {
		return mode.get().equals("vertical");
	}

	@Override
	protected void drawNanoVG() {
		this.setWidth(vertical() ? 16 : 16 * 4);
		this.setHeight(vertical() ? 16 * 4 : 16);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		ItemStack[] items = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			if (isEditing() || mc.player == null) {
				items[i] = new ItemStack(i == 0 ? Items.DIAMOND_HELMET : i == 1 ? Items.DIAMOND_CHESTPLATE : i == 2 ? Items.DIAMOND_LEGGINGS : Items.DIAMOND_BOOTS);
			} else {
				items[i] = mc.player.getEquippedStack(SLOTS[i]);
			}
		}
		MatrixStack m = context.getMatrices();
		m.push();
		m.translate(px, py, 0);
		m.scale(scale, scale, 1F);
		for (int i = 0; i < 4; i++) {
			ItemStack item = items[i];
			int addX = vertical() ? 0 : 16 * i;
			int addY = vertical() ? 16 * i : 0;
			if (!item.isEmpty()) {
				context.drawItem(item, addX, addY);
				if (value.enabled()) {
					context.drawItemInSlot(mc.textRenderer, item, addX, addY, "");
				}
			}
		}
		m.pop();
	}
}
