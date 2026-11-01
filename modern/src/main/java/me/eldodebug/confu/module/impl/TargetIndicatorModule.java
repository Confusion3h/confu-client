package me.eldodebug.confu.module.impl;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.utils.ColorUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** 1.8.9 TargetIndicatorMod: animated ring around the last crosshair player. */
public final class TargetIndicatorModule extends Module {

	private static final long TARGET_TIMEOUT = 2500L;
	private static final Map<String, Color> COLORS = new LinkedHashMap<>();

	static {
		COLORS.put("Red", new Color(255, 0, 0));
		COLORS.put("Orange", new Color(255, 128, 0));
		COLORS.put("Yellow", new Color(255, 255, 0));
		COLORS.put("Green", new Color(0, 255, 0));
		COLORS.put("Cyan", new Color(0, 255, 255));
		COLORS.put("Blue", new Color(0, 0, 255));
		COLORS.put("Purple", new Color(128, 0, 255));
		COLORS.put("Pink", new Color(255, 105, 180));
		COLORS.put("White", new Color(255, 255, 255));
	}

	private static boolean registered;
	private final BooleanSetting customColor = bool("custom_color", "tt:CUSTOM_COLOR", false);
	private final ComboSetting color = combo("color", "tt:COLOR", "Red", COLORS.keySet().toArray(new String[0]));
	private AbstractClientPlayerEntity target;
	private long targetTime;

	public TargetIndicatorModule() {
		super("target_indicator", Category.RENDER, "tt:TARGET_INDICATOR", "tt:TARGET_INDICATOR_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(TargetIndicatorModule::render);
		}
	}

	@Override
	public void onTick() {
		if (mc.world == null || mc.player == null) {
			target = null;
			return;
		}
		if (mc.crosshairTarget instanceof EntityHitResult hit
				&& hit.getEntity() instanceof AbstractClientPlayerEntity player
				&& player != target && inTablist(player)) {
			target = player;
			targetTime = System.currentTimeMillis();
		} else if (mc.crosshairTarget == null && System.currentTimeMillis() - targetTime > TARGET_TIMEOUT) {
			target = null;
		}
		if (target != null) {
			if (!target.isAlive() || !mc.player.isAlive() || target.isInvisible()
					|| target.squaredDistanceTo(mc.player) > 144) {
				target = null;
			}
		}
	}

	private boolean inTablist(PlayerEntity player) {
		if (mc.getNetworkHandler() == null) {
			return false;
		}
		for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
			if (entry.getProfile().getId().equals(player.getUuid())) {
				return true;
			}
		}
		return false;
	}

	private Color activeColor() {
		if (customColor.enabled()) {
			return ColorUtils.applyAlpha(COLORS.getOrDefault(color.get(), Color.RED), 255);
		}
		AccentColor accent = Glide.getInstance().getColorManager().getCurrentColor();
		return accent.getInterpolateColor();
	}

	private static void render(WorldRenderContext ctx) {
		TargetIndicatorModule mod;
		try {
			mod = me.eldodebug.confu.ConfuClient.get().modules().get(TargetIndicatorModule.class);
		} catch (RuntimeException e) {
			return;
		}
		AbstractClientPlayerEntity target = mod.target;
		if (!mod.isEnabled() || target == null) {
			return;
		}
		float tickDelta = ctx.tickDelta();
		Vec3d cam = ctx.camera().getPos();
		long now = System.currentTimeMillis();
		double x = MathHelper.lerp(tickDelta, target.prevX, target.getX()) - cam.x;
		double y = MathHelper.lerp(tickDelta, target.prevY, target.getY()) - cam.y + Math.sin(now / 200D) + 1;
		double z = MathHelper.lerp(tickDelta, target.prevZ, target.getZ()) - cam.z;
		Color color = mod.activeColor();

		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		float bob = (float) (Math.cos(now / 200D) / 2.0F);
		for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / 64F) {
			double vx = x + 0.67 * Math.cos(i);
			double vz = z + 0.67 * Math.sin(i);
			buffer.vertex(matrix, (float) vx, (float) (y - bob), (float) vz)
					.color(color.getRed(), color.getGreen(), color.getBlue(), 0).next();
			buffer.vertex(matrix, (float) vx, (float) y, (float) vz)
					.color(color.getRed(), color.getGreen(), color.getBlue(), 102).next();
		}
		tessellator.draw();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}
