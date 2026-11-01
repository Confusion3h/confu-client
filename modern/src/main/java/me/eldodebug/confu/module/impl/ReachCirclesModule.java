package me.eldodebug.confu.module.impl;

import java.awt.Color;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 ReachCirclesMod: circle around the player at reach distance. */
public final class ReachCirclesModule extends Module {

	private static boolean registered;
	private final NumberSetting radius = number("radius", "tt:RANGE", 3, 1, 6, 0.1);

	public ReachCirclesModule() {
		super("reach_circles", Category.PLAYER, "tt:REACH_CIRCLES", "tt:REACH_CIRCLES_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(ReachCirclesModule::render);
		}
	}

	private static void render(WorldRenderContext ctx) {
		ReachCirclesModule mod;
		try { mod = me.eldodebug.confu.ConfuClient.get().modules().get(ReachCirclesModule.class); }
		catch (RuntimeException e) { return; }
		if (!mod.isEnabled() || mod.mc.player == null) return;
		float r = mod.radius.getFloat();
		var p = mod.mc.player;
		float td = ctx.tickDelta();
		double x = MathHelper.lerp(td, p.prevX, p.getX()) - ctx.camera().getPos().x;
		double y = MathHelper.lerp(td, p.prevY, p.getY()) - ctx.camera().getPos().y + 0.1;
		double z = MathHelper.lerp(td, p.prevZ, p.getZ()) - ctx.camera().getPos().z;
		Matrix4f m = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2F);
		buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		Color c = Color.CYAN;
		for (int i = 0; i <= 64; i++) {
			double a = i / 64.0 * Math.PI * 2;
			buf.vertex(m, (float)(x + Math.cos(a)*r), (float)y, (float)(z + Math.sin(a)*r))
				.color(c.getRed(), c.getGreen(), c.getBlue(), 180).next();
		}
		tess.draw();
		RenderSystem.disableBlend();
	}
}
