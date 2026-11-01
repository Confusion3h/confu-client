package me.eldodebug.confu.module.impl;

import java.util.Map;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

/** 1.8.9 ItemInfoMod: held item enchants / potion effects above the hotbar. */
public final class ItemInfoModule extends Module {

	private final BooleanSetting potionColor = bool("potion_color", "tt:POTION_COLOR", false);

	public ItemInfoModule() {
		super("item_info", Category.RENDER, "tt:ITEM_INFO", "tt:ITEM_INFO_DESCRIPTION");
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		if (mc.player == null) {
			return;
		}
		ItemStack stack = mc.player.getMainHandStack();
		if (stack.isEmpty()) {
			return;
		}

		String text = stack.getItem() instanceof PotionItem ? potionEffects(stack) : enchantments(stack);
		if (text.isEmpty()) {
			return;
		}

		int addY = mc.player.isCreative() ? 55 : 70;
		int width = mc.getWindow().getScaledWidth();
		int height = mc.getWindow().getScaledHeight();
		int x = (width / 2) - (mc.textRenderer.getWidth(text) / 2);
		context.drawText(mc.textRenderer, text, x, height - addY - 1, 0xFFFFFFFF, true);
	}

	private String potionEffects(ItemStack stack) {
		StringBuilder builder = new StringBuilder();
		for (StatusEffectInstance effect : PotionUtil.getPotionEffects(stack)) {
			if (potionColor.enabled()) {
				builder.append(effectColor(effect));
			}
			builder.append(Text.translatable(effect.getTranslationKey()).getString());
			builder.append("§f ");
			builder.append(effect.getAmplifier() + 1);
			builder.append(" (");
			int seconds = effect.getDuration() / 20;
			builder.append(seconds / 60).append(String.format(":%02d", seconds % 60));
			builder.append(") ");
		}
		return builder.toString().trim();
	}

	private String effectColor(StatusEffectInstance effect) {
		if (effect.getEffectType() == StatusEffects.SPEED) return "§b";
		if (effect.getEffectType() == StatusEffects.REGENERATION) return "§d";
		if (effect.getEffectType() == StatusEffects.POISON) return "§2";
		if (effect.getEffectType() == StatusEffects.JUMP_BOOST) return "§a";
		if (effect.getEffectType() == StatusEffects.FIRE_RESISTANCE) return "§6";
		if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) return "§c";
		if (effect.getEffectType() == StatusEffects.SLOWNESS) return "§7";
		if (effect.getEffectType() == StatusEffects.NIGHT_VISION) return "§1";
		if (effect.getEffectType() == StatusEffects.STRENGTH) return "§5";
		return "";
	}

	private String enchantments(ItemStack stack) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(stack).entrySet()) {
			builder.append(shortName(entry.getKey()));
			builder.append(' ');
			builder.append(entry.getValue());
			builder.append(' ');
		}
		return builder.toString().trim();
	}

	private String shortName(Enchantment enchantment) {
		var id = Registries.ENCHANTMENT.getId(enchantment);
		String path = id == null ? "" : id.getPath();
		return switch (path) {
			case "protection" -> "P";
			case "fire_protection" -> "FP";
			case "feather_falling" -> "FF";
			case "blast_protection" -> "BP";
			case "projectile_protection" -> "PP";
			case "respiration" -> "R";
			case "aqua_affinity" -> "AA";
			case "thorns" -> "T";
			case "depth_strider" -> "DS";
			case "frost_walker" -> "FW";
			case "sharpness" -> "SH";
			case "smite" -> "SM";
			case "bane_of_arthropods" -> "BoA";
			case "knockback" -> "KB";
			case "fire_aspect" -> "FA";
			case "looting" -> "L";
			case "efficiency" -> "EFF";
			case "silk_touch" -> "ST";
			case "unbreaking" -> "UNB";
			case "fortune" -> "F";
			case "power" -> "POW";
			case "punch" -> "PUN";
			case "flame" -> "FLA";
			case "infinity" -> "INF";
			case "luck_of_the_sea" -> "LoS";
			case "lure" -> "LU";
			case "mending" -> "MEN";
			default -> path.isEmpty() ? "?" : path.substring(0, Math.min(3, path.length())).toUpperCase();
		};
	}
}
