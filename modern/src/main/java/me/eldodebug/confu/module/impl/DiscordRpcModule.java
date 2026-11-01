package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.discord.DiscordRPC;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.StringSetting;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.notification.NotificationType;

/** 1.8.9 DiscordRPCMod: rich presence through the user's own Discord application. */
public final class DiscordRpcModule extends Module {

	private final DiscordRPC discord = new DiscordRPC();
	private final StringSetting appId = string("app_id", "tt:DISCORD_APP_ID", "");

	public DiscordRpcModule() {
		super("discord_rpc", Category.OTHER, "tt:DISCORD_RPC", "tt:DISCORD_RPC_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		String id = appId.get().trim();
		if (!id.matches("\\d{17,20}")) {
			Glide.getInstance().getNotificationManager().post("Discord RPC",
					"Set your Discord Application ID in this mod's settings", NotificationType.WARNING);
			return;
		}
		discord.start(Long.parseLong(id));
	}

	@Override
	protected void onDisable() {
		if (discord.isStarted()) {
			discord.stop();
		}
	}
}
