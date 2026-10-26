package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.item.Items;

/** New for 1.20.1: totems of undying in your inventory and off hand, in the 1.8.9 HUD style. */
public final class TotemCounterModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final BooleanSetting hideEmpty = bool("hide_empty", "setting.totem.hide_empty", false);

	public TotemCounterModule() {
		super("totem_counter", "module.totem.name", "module.totem.desc", 0F, 0.50F);
	}

	private int count() {
		if (mc.player == null) {
			return 0;
		}
		int n = 0;
		var inv = mc.player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
				n += inv.getStack(i).getCount();
			}
		}
		return n;
	}

	@Override
	protected boolean visible() {
		return !hideEmpty.enabled() || count() > 0;
	}

	@Override
	public String getText() {
		int n = count();
		return n + (n == 1 ? " Totem" : " Totems");
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.HEART_FILL : null;
	}
}
