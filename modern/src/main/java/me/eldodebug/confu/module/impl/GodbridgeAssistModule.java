package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 GodbridgeAssistMod: yaw/pitch assist HUD for bridging. */
public final class GodbridgeAssistModule extends NvgHudModule {

	public GodbridgeAssistModule() {
		super("godbridge_assist", Category.HUD, "tt:GODBRIDGE_ASSIST", "tt:GODBRIDGE_ASSIST_DESCRIPTION", 0.5F, 0.75F);
	}

	@Override
	protected void drawNanoVG() {
		float yaw = mc.player == null ? 0 : MathHelper.wrapDegrees(mc.player.getYaw());
		float pitch = mc.player == null ? 0 : mc.player.getPitch();
		// Classic godbridge targets ~78 pitch / diagonal yaw
		boolean goodPitch = pitch > 75 && pitch < 82;
		boolean goodYaw = Math.abs(Math.abs(yaw % 90) - 45) < 5;
		String status = goodPitch && goodYaw ? "Ready" : "Adjust";
		this.drawBackground(70, 28);
		this.drawText(status, 5.5F, 5.5F, 9, getHudFont(1));
		this.drawText(String.format("%.0f / %.0f", yaw, pitch), 5.5F, 15.5F, 8, getHudFont(1));
		this.setWidth(70);
		this.setHeight(28);
	}
}
