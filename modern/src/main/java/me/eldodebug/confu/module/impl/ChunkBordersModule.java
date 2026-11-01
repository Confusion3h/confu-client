package me.eldodebug.confu.module.impl;

import java.awt.Color;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 ChunkBordersMod: blue chunk edges + yellow section grid. */
public final class ChunkBordersModule extends Module {

	private static boolean registered;

	public ChunkBordersModule() {
		super("chunk_borders", Category.RENDER, "tt:CHUNK_BORDERS", "tt:CHUNK_BORDERS_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(ChunkBordersModule::render);
		}
	}

	private static void render(WorldRenderContext ctx) {
		ChunkBordersModule mod;
		try {
			mod = me.eldodebug.confu.ConfuClient.get().modules().get(ChunkBordersModule.class);
		} catch (RuntimeException e) {
			return;
		}
		if (!mod.isEnabled()) {
			return;
		}
		ClientPlayerEntity player = mod.mc.player;
		if (player == null) {
			return;
		}
		float frame = ctx.tickDelta();
		double camX = MathHelper.lerp(frame, player.prevX, player.getX());
		double camY = MathHelper.lerp(frame, player.prevY, player.getY());
		double camZ = MathHelper.lerp(frame, player.prevZ, player.getZ());
		int chunkX = player.getBlockX() >> 4;
		int chunkZ = player.getBlockZ() >> 4;

		Color blue = Color.BLUE;
		Color yellow = Color.YELLOW;

		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.lineWidth(1F);
		buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

		double baseX = chunkX * 16.0 - camX;
		double baseZ = chunkZ * 16.0 - camZ;
		double baseY = -camY;

		for (int ez = -2; ez <= 2; ez++) {
			for (int ex = -2; ex <= 2; ex++) {
				if (Math.abs(ex) == 2 && Math.abs(ez) == 2) {
					continue;
				}
				double x = baseX + ex * 16.0;
				double z = baseZ + ez * 16.0;
				vert(buf, matrix, x, baseY, z, blue);
				vert(buf, matrix, x, baseY + 256, z, blue);
				vert(buf, matrix, x + 16, baseY, z, blue);
				vert(buf, matrix, x + 16, baseY + 256, z, blue);
				vert(buf, matrix, x, baseY, z + 16, blue);
				vert(buf, matrix, x, baseY + 256, z + 16, blue);
				vert(buf, matrix, x + 16, baseY, z + 16, blue);
				vert(buf, matrix, x + 16, baseY + 256, z + 16, blue);
			}
		}

		double x0 = baseX;
		double z0 = baseZ;
		for (int y = 0; y < 257; y++) {
			if (y < 256) {
				for (int n = 1; n < 16; n++) {
					vert(buf, matrix, x0 + n, baseY + y, z0, yellow);
					vert(buf, matrix, x0 + n, baseY + y + 1, z0, yellow);
					vert(buf, matrix, x0, baseY + y, z0 + n, yellow);
					vert(buf, matrix, x0, baseY + y + 1, z0 + n, yellow);
					vert(buf, matrix, x0 + n, baseY + y, z0 + 16, yellow);
					vert(buf, matrix, x0 + n, baseY + y + 1, z0 + 16, yellow);
					vert(buf, matrix, x0 + 16, baseY + y, z0 + n, yellow);
					vert(buf, matrix, x0 + 16, baseY + y + 1, z0 + n, yellow);
				}
			}
			vert(buf, matrix, x0, baseY + y, z0, yellow);
			vert(buf, matrix, x0 + 16, baseY + y, z0, yellow);
			vert(buf, matrix, x0, baseY + y, z0, yellow);
			vert(buf, matrix, x0, baseY + y, z0 + 16, yellow);
			vert(buf, matrix, x0 + 16, baseY + y, z0, yellow);
			vert(buf, matrix, x0 + 16, baseY + y, z0 + 16, yellow);
			vert(buf, matrix, x0, baseY + y, z0 + 16, yellow);
			vert(buf, matrix, x0 + 16, baseY + y, z0 + 16, yellow);
		}

		tess.draw();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void vert(BufferBuilder buf, Matrix4f matrix, double x, double y, double z, Color c) {
		buf.vertex(matrix, (float) x, (float) y, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
	}
}
