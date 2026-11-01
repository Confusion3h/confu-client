package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.client.network.PlayerListEntry;

/** 1.8.9 PlayerListMod: tab-style player list with heads on the HUD. */
public final class PlayerListModule extends NvgHudModule {

	private final NumberSetting max = number("max", "tt:MAX", 16, 1, 100, 1);

	private int index;
	private float maxName;

	public PlayerListModule() {
		super("player_list", Category.HUD, "tt:PLAYER_LIST", "tt:PLAYER_LIST_DESCRIPTION", 0.02F, 0.30F);
	}

	@Override
	protected void drawNanoVG() {
		int prevIndex = 0;
		int offsetY = 23;

		this.drawBackground(maxName, (index * 15) + 24.5F);
		this.drawText("Player List", 5.5F, 6F, 10.5F, getHudFont(1));
		this.drawRect(0, 18, maxName, 1, getFontColor());

		if (mc.getNetworkHandler() != null) {
			for (PlayerListEntry playerInfo : mc.getNetworkHandler().getPlayerList()) {
				if (playerInfo != null && playerInfo.getProfile() != null) {
					String name = playerInfo.getProfile().getName();
					float width = this.getTextWidth(name, 9, getHudFont(2)) + 26;
					if (width > maxName) {
						maxName = width;
					}
					if (playerInfo.getSkinTexture() != null) {
						this.drawPlayerHead(playerInfo.getSkinTexture(), 5.5F, offsetY, 12, 12, 2.5F);
					}
					this.drawText(name, 20, offsetY + 2.5F, 9, getHudFont(1));

					if (prevIndex > max.getInt()) {
						prevIndex++;
						index = prevIndex;
						break;
					}

					prevIndex++;
					offsetY += 15;
				}
			}
		}

		index = prevIndex;

		this.setWidth((int) maxName);
		this.setHeight((index * 15) + 26);
	}
}
