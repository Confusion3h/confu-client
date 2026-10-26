package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.HudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ArmorStatusModule extends HudModule {

	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
	private static final int ROW = 18;
	private static final int W = 64;

	private final BooleanSetting heldItem = bool("held", "setting.armor.held", true);
	private final BooleanSetting percent = bool("percent", "setting.armor.percent", false);
	private final NumberSetting scale = number("scale", "setting.hud.scale", 1, 0.5, 2, 0.05);

	public ArmorStatusModule() {
		super("armor_status", Category.HUD, "module.armor.name", "module.armor.desc", 1F, 0.5F);
	}

	private ItemStack[] stacks() {
		ItemStack[] out = new ItemStack[SLOTS.length];
		for (int i = 0; i < SLOTS.length; i++) {
			if (mc.player == null) {
				out[i] = ItemStack.EMPTY;
			} else {
				out[i] = mc.player.getEquippedStack(SLOTS[i]);
			}
		}
		if (mc.player == null) {
			out = new ItemStack[]{new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
					new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS), new ItemStack(Items.DIAMOND_SWORD)};
		}
		return out;
	}

	private int rows() {
		return heldItem.enabled() ? SLOTS.length : SLOTS.length - 1;
	}

	@Override
	protected int width() {
		return W;
	}

	@Override
	protected int height() {
		return rows() * ROW + 4;
	}

	@Override
	protected float scale() {
		return scale.getFloat();
	}

	@Override
	protected void draw(DrawContext context, float tickDelta) {
		ItemStack[] stacks = stacks();
		Draw.roundRect(context, 0, 0, W, height(), 5, Theme.KEY);
		for (int i = 0; i < rows(); i++) {
			ItemStack stack = stacks[i];
			int y = 2 + i * ROW;
			if (stack.isEmpty()) {
				continue;
			}
			context.drawItem(stack, 3, y);
			String label;
			if (stack.isDamageable()) {
				int left = stack.getMaxDamage() - stack.getDamage();
				label = percent.enabled() ? Math.round(100F * left / stack.getMaxDamage()) + "%" : String.valueOf(left);
				float ratio = left / (float) stack.getMaxDamage();
				int color = Theme.lerp(0xFFE5484D, Theme.ACCENT, ratio);
				context.drawText(mc.textRenderer, label, 23, y + 5, color, false);
			} else {
				context.drawText(mc.textRenderer, String.valueOf(stack.getCount()), 23, y + 5, Theme.TEXT, false);
			}
		}
	}
}
