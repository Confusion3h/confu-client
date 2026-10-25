package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.discord.DiscordRPC;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;
import me.eldodebug.soar.management.notification.NotificationType;

/**
 * Discord Rich Presence under your own Discord application (create one at
 * discord.com/developers, name it Confu Client, upload an asset called "icon").
 * The upstream hardcoded application belonged to Glide and showed its name.
 */
public class DiscordRPCMod extends Mod {

	private DiscordRPC discord = new DiscordRPC();
	private final TextSetting appIdSetting = new TextSetting(TranslateText.DISCORD_APP_ID, this, "");

	public DiscordRPCMod() {
		super(TranslateText.DISCORD_RPC, TranslateText.DISCORD_RPC_DESCRIPTION, ModCategory.OTHER);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		String id = appIdSetting.getText().trim();
		if (!id.matches("\\d{17,20}")) {
			Glide.getInstance().getNotificationManager().post("Discord RPC", "Set your Discord Application ID in this mod's settings", NotificationType.WARNING);
			return;
		}
		discord.start(Long.parseLong(id));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		if(discord.isStarted()) {
			discord.stop();
		}
	}
}
