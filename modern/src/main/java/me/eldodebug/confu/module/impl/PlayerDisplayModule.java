package me.eldodebug.confu.module.impl;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;

/** 1.8.9 PlayerDisplayMod: the local player rendered as a paperdoll on the HUD. */
public final class PlayerDisplayModule extends NvgHudModule {

	private final NumberSetting yawOffset = number("yaw_offset", "tt:YAW_OFFSET", 0, -90, 120, 1);

	public PlayerDisplayModule() {
		super("player_display", Category.HUD, "tt:PLAYER_DISPLAY", "tt:PLAYER_DISPLAY_DESCRIPTION", 0.02F, 0.50F);
	}

	@Override
	protected void drawNanoVG() {
		this.setWidth(30);
		this.setHeight(60);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		PlayerEntity player = mc.player;
		if (player == null) {
			return;
		}
		float partial = mc.getTickDelta();
		MatrixStack matrices = context.getMatrices();
		VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

		matrices.push();
		matrices.translate(px + 15F * scale, py + 58F * scale, 50D);
		matrices.scale(-scale * 30F, scale * 30F, scale * 30F);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180F));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(player.getYaw(partial) + yawOffset.getFloat()));

		RenderSystem.enableDepthTest();
		mc.getEntityRenderDispatcher().setRenderShadows(false);
		mc.getEntityRenderDispatcher().render(player, 0D, 0D, 0D, 0F, partial, matrices, immediate,
				LightmapTextureManager.MAX_LIGHT_COORDINATE);
		immediate.draw();
		mc.getEntityRenderDispatcher().setRenderShadows(true);
		RenderSystem.disableDepthTest();
		matrices.pop();
	}
}
