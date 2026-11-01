package me.eldodebug.confu.module.impl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.impl.minimap.ChunkAtlas;
import me.eldodebug.confu.module.impl.minimap.ChunkTile;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.utils.buffer.ScreenStencil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 MinimapMod: rotating chunk atlas map, clipped by a rounded stencil. */
public final class MinimapModule extends NvgHudModule {

	private final NumberSetting widthSetting = number("width", "tt:WIDTH", 150, 10, 180, 1);
	private final NumberSetting heightSetting = number("height", "tt:HEIGHT", 70, 10, 180, 1);
	private final NumberSetting alphaSetting = number("alpha", "tt:ALPHA", 1, 0, 1, 0.01);

	private final ScreenStencil stencil = new ScreenStencil();
	private ChunkAtlas chunkAtlas;
	private ClientWorld lastWorld;

	public MinimapModule() {
		super("minimap", Category.HUD, "tt:MINIMAP", "tt:MINIMAP_DESCRIPTION", 0.02F, 0.02F);
	}

	@Override
	protected int width() {
		return widthSetting.getInt();
	}

	@Override
	protected int height() {
		return heightSetting.getInt();
	}

	@Override
	protected void drawNanoVG() {
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		if (!visible() && !isEditing()) {
			return;
		}
		int sw = mc.getWindow().getScaledWidth();
		int sh = mc.getWindow().getScaledHeight();
		int px = Math.round(x() * Math.max(0F, sw - scaledWidth()));
		int py = Math.round(y() * Math.max(0F, sh - scaledHeight()));
		float s = scale();
		int width = widthSetting.getInt();
		int height = heightSetting.getInt();

		context.draw();
		nvg().setupAndDraw(() -> nvg().drawShadow(px, py, width * s, height * s, 6 * s));
		stencil.wrap(() -> drawMap(tickDelta, px, py, width * s, height * s), px, py, width * s, height * s, 6 * s, alphaSetting.getFloat());
	}

	@Override
	protected void onEnable() {
		if (chunkAtlas != null) {
			chunkAtlas.clear();
		}
		lastWorld = null;
	}

	private void drawMap(float partialTicks, float px, float py, float width, float height) {

		if (mc.player == null || mc.world == null) {
			return;
		}

		if (lastWorld != mc.world) {
			lastWorld = mc.world;
			if (chunkAtlas != null) {
				chunkAtlas.clear();
			}
		}

		if (chunkAtlas == null) {
			chunkAtlas = new ChunkAtlas(10);
		}

		double x = MathHelper.lerp(partialTicks, mc.player.prevX, mc.player.getX());
		double z = MathHelper.lerp(partialTicks, mc.player.prevZ, mc.player.getZ());
		double yaw = mc.player.getYaw(partialTicks);

		chunkAtlas.loadChunks((int) x >> 4, (int) z >> 4);

		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		buffer.vertex(px, py, 0F).color(138, 176, 254, 255).next();
		buffer.vertex(px, py + height, 0F).color(138, 176, 254, 255).next();
		buffer.vertex(px + width, py + height, 0F).color(138, 176, 254, 255).next();
		buffer.vertex(px + width, py, 0F).color(138, 176, 254, 255).next();
		tessellator.draw();

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, chunkAtlas.getTextureHandle());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		Matrix4f matrix = new Matrix4f()
				.translate(px + width / 2F, py + height / 2F, 0F)
				.rotateZ((float) Math.toRadians(180.0 - yaw));

		double chunkWidth = chunkAtlas.getSpriteWidth();
		double chunkHeight = chunkAtlas.getSpriteHeight();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		for (ChunkTile sprite : chunkAtlas) {

			double minX = chunkAtlas.getSpriteX(sprite.getOffset());
			double minY = chunkAtlas.getSpriteY(sprite.getOffset());

			double maxX = minX + chunkWidth;
			double maxY = minY + chunkHeight;

			double renderX = (sprite.getChunkX() << 4) - x;
			double renderY = (sprite.getChunkZ() << 4) - z;

			buffer.vertex(matrix, (float) renderX, (float) renderY, 0F).texture((float) minX, (float) minY).next();
			buffer.vertex(matrix, (float) renderX, (float) (renderY + 16), 0F).texture((float) minX, (float) maxY).next();
			buffer.vertex(matrix, (float) (renderX + 16), (float) (renderY + 16), 0F).texture((float) maxX, (float) maxY).next();
			buffer.vertex(matrix, (float) (renderX + 16), (float) renderY, 0F).texture((float) maxX, (float) minY).next();
		}

		tessellator.draw();
	}
}
