package me.eldodebug.confu.module.impl;

import java.awt.Color;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.TimerUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** 1.8.9 PlayerPredicatorMod: box at the server position of the crosshair target. */
public final class PlayerPredicatorModule extends Module {

	private static boolean registered;

	private final TimerUtils timer = new TimerUtils();
	private Position realTargetPosition = new Position(0, 0, 0);
	private AbstractClientPlayerEntity target;
	private boolean isActive;

	public PlayerPredicatorModule() {
		super("player_predicator", Category.WORLD, "tt:PLAYER_PREDICATOR", "tt:PLAYER_PREDICATOR_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(PlayerPredicatorModule::render);
		}
	}

	@Override
	public void onTick() {

		if (mc.world == null || mc.player == null) {
			target = null;
			isActive = true;
			return;
		}

		updateTarget();

		if (target == null) {
			isActive = true;
			return;
		}

		if (isActive) {
			realTargetPosition = new Position(target.getX(), target.getY(), target.getZ());
			isActive = false;
		}
	}

	private void updateTarget() {

		if (mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() != target) {
			if (hit.getEntity() instanceof AbstractClientPlayerEntity player && isInTablist(player)) {
				target = player;
				timer.reset();
			}
		} else if (mc.crosshairTarget == null && timer.delay(2500)) {
			target = null;
			timer.reset();
		}

		if (target != null) {
			if (!target.isAlive() || !mc.player.isAlive()) {
				target = null;
			} else if (target.isInvisible() || target.distanceTo(mc.player) > 12) {
				target = null;
			}
		}
	}

	private boolean isInTablist(AbstractClientPlayerEntity player) {
		if (mc.isInSingleplayer() || mc.getCurrentServerEntry() == null) {
			return false;
		}
		ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
		if (networkHandler == null) {
			return false;
		}
		for (PlayerListEntry entry : networkHandler.getPlayerList()) {
			if (entry.getProfile() != null && entry.getProfile().getName().contains(player.getName().getString())) {
				return true;
			}
		}
		return false;
	}

	public static void handleEntityPacket(EntityS2CPacket packet, ClientWorld world) {
		PlayerPredicatorModule mod = instance();
		if (mod == null || !mod.isEnabled() || mod.target == null) {
			return;
		}
		Entity entity = packet.getEntity(world);
		if (entity != null && entity == mod.target) {
			mod.realTargetPosition.x += packet.getDeltaX() / 4096D;
			mod.realTargetPosition.y += packet.getDeltaY() / 4096D;
			mod.realTargetPosition.z += packet.getDeltaZ() / 4096D;
		}
	}

	public static void handleTeleportPacket(EntityPositionS2CPacket packet) {
		PlayerPredicatorModule mod = instance();
		if (mod == null || !mod.isEnabled() || mod.target == null) {
			return;
		}
		mod.realTargetPosition = new Position(packet.getX(), packet.getY(), packet.getZ());
	}

	private static PlayerPredicatorModule instance() {
		try {
			return me.eldodebug.confu.ConfuClient.get().modules().get(PlayerPredicatorModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static boolean isHypixel() {
		return mcServerAddress() != null && mcServerAddress().contains("hypixel");
	}

	private static String mcServerAddress() {
		return net.minecraft.client.MinecraftClient.getInstance().getCurrentServerEntry() == null ? null
				: net.minecraft.client.MinecraftClient.getInstance().getCurrentServerEntry().address;
	}

	private static void render(WorldRenderContext ctx) {
		PlayerPredicatorModule mod;
		try {
			mod = me.eldodebug.confu.ConfuClient.get().modules().get(PlayerPredicatorModule.class);
		} catch (RuntimeException e) {
			return;
		}
		AbstractClientPlayerEntity target = mod.target;
		if (!mod.isEnabled() || target == null || mod.mc.player == null) {
			return;
		}
		if (mod.realTargetPosition.squareDistanceTo(target.getX(), target.getY(), target.getZ()) <= 0.00001 || isHypixel()) {
			return;
		}

		var player = mod.mc.player;
		Box box = player.getBoundingBox()
				.offset(-player.getX(), -player.getY(), -player.getZ())
				.offset(mod.realTargetPosition.x, mod.realTargetPosition.y, mod.realTargetPosition.z)
				.expand(0.14D);

		Color color = ColorUtils.applyAlpha(Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor(0), 80);
		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		Vec3d cam = ctx.camera().getPos();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		tessellator.draw();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		tessellator.draw();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		tessellator.draw();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		tessellator.draw();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		tessellator.draw();

		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.maxZ);
		vert(buffer, matrix, cam, color, box.minX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.minX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.minZ);
		vert(buffer, matrix, cam, color, box.maxX, box.maxY, box.maxZ);
		vert(buffer, matrix, cam, color, box.maxX, box.minY, box.maxZ);
		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void vert(BufferBuilder buffer, Matrix4f matrix, Vec3d cam, Color color, double x, double y, double z) {
		buffer.vertex(matrix, (float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z))
				.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
	}

	private static class Position {

		private double x, y, z;

		private Position(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		private double squareDistanceTo(double x, double y, double z) {
			double d0 = x - this.x;
			double d1 = y - this.y;
			double d2 = z - this.z;
			return d0 * d0 + d1 * d1 + d2 * d2;
		}
	}
}
