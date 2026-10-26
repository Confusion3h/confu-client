package me.eldodebug.soar.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;

public class ServerUtils {

	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean isInTablist(Entity entity) {
		if(isJoinServer()) {
			for (NetworkPlayerInfo item : mc.getNetHandler().getPlayerInfoMap()) {
				NetworkPlayerInfo playerInfo = (NetworkPlayerInfo) item;

				if (playerInfo != null && playerInfo.getGameProfile() != null && playerInfo.getGameProfile().getName().contains(entity.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isJoinServer() {
		return !mc.isSingleplayer() && mc.getCurrentServerData() != null;
	}
	
	public static String getServerIP() {
		
		String ip;
		
		if(isJoinServer()) {
			ip = mc.getCurrentServerData().serverIP;
		}else {
			ip = "Single Player";
		}
		
		return ip;
	}
	
	public static int getPing() {
		if(mc.isSingleplayer() || !isJoinServer()) {
			return 0;
		}else {
			// ServerData.pingToServer is only filled by the multiplayer server-list
			// pinger, so it stays -1 on direct connect / quick join. The live value
			// is the tab-list latency the server sends for our own player.
			if(mc.thePlayer != null && mc.getNetHandler() != null) {
				NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
				if(info != null && info.getResponseTime() > 0) {
					return info.getResponseTime();
				}
			}
			long listPing = mc.getCurrentServerData().pingToServer;
			return listPing > 0 ? (int) listPing : 0;
		}
	}
	
	public static boolean isHypixel() {
		return getServerIP().contains("hypixel");
	}
	
	public static boolean isMinemen() {
		return getServerIP().contains("minemen");
	}
}
