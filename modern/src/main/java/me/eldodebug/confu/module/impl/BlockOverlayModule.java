package me.eldodebug.confu.module.impl;

import java.awt.Color;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.Glide;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

/** 1.8.9 BlockOverlayMod (outline on looked-at block; cancels vanilla). */
public final class BlockOverlayModule extends Module {

	private static boolean registered;
	private final BooleanSetting outline = bool("outline", "tt:OUTLINE", true);
	private final NumberSetting outlineAlpha = number("outline_alpha", "tt:OUTLINE_ALPHA", 0.15, 0, 1, 0.05);
	private final NumberSetting outlineWidth = number("outline_width", "tt:OUTLINE_WIDTH", 4, 1, 10, 0.5);

	public BlockOverlayModule() {
		super("block_overlay", Category.RENDER, "tt:BLOCK_OVERLAY", "tt:BLOCK_OVERLAY_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hit) -> {
				BlockOverlayModule mod;
				try {
					mod = me.eldodebug.confu.ConfuClient.get().modules().get(BlockOverlayModule.class);
				} catch (RuntimeException e) {
					return true;
				}
				if (!mod.isEnabled()) {
					return true;
				}
				mod.draw(ctx, hit);
				return false;
			});
		}
	}

	private void draw(WorldRenderContext ctx, HitResult hit) {
		if (!outline.enabled() || !(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK || mc.world == null) {
			return;
		}
		BlockPos pos = bhr.getBlockPos();
		VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
		if (shape.isEmpty()) {
			return;
		}
		Box box = shape.getBoundingBox().offset(pos).expand(0.002);
		Vec3d cam = ctx.camera().getPos();
		box = box.offset(-cam.x, -cam.y, -cam.z);
		Color accent = Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor();
		int a = Math.max(30, (int) (outlineAlpha.getFloat() * 255));
		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.disableCull();
		RenderSystem.lineWidth(outlineWidth.getFloat());
		buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		lineBox(buf, matrix, box, accent.getRed(), accent.getGreen(), accent.getBlue(), a);
		tess.draw();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void lineBox(BufferBuilder buf, Matrix4f m, Box b, int r, int g, int bl, int a) {
		float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ;
		float x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;
		edge(buf, m, x0, y0, z0, x1, y0, z0, r, g, bl, a);
		edge(buf, m, x1, y0, z0, x1, y0, z1, r, g, bl, a);
		edge(buf, m, x1, y0, z1, x0, y0, z1, r, g, bl, a);
		edge(buf, m, x0, y0, z1, x0, y0, z0, r, g, bl, a);
		edge(buf, m, x0, y1, z0, x1, y1, z0, r, g, bl, a);
		edge(buf, m, x1, y1, z0, x1, y1, z1, r, g, bl, a);
		edge(buf, m, x1, y1, z1, x0, y1, z1, r, g, bl, a);
		edge(buf, m, x0, y1, z1, x0, y1, z0, r, g, bl, a);
		edge(buf, m, x0, y0, z0, x0, y1, z0, r, g, bl, a);
		edge(buf, m, x1, y0, z0, x1, y1, z0, r, g, bl, a);
		edge(buf, m, x1, y0, z1, x1, y1, z1, r, g, bl, a);
		edge(buf, m, x0, y0, z1, x0, y1, z1, r, g, bl, a);
	}

	private static void edge(BufferBuilder buf, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int a) {
		buf.vertex(m, x0, y0, z0).color(r, g, b, a).next();
		buf.vertex(m, x1, y1, z1).color(r, g, b, a).next();
	}
}
