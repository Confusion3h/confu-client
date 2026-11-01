package me.eldodebug.confu.module.impl;

import java.awt.Color;
import java.util.ArrayDeque;

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
import net.minecraft.util.math.Vec3d;

/** 1.8.9 BreadcrumbsMod: path trail. */
public final class BreadcrumbsModule extends Module {

	private static boolean registered;
	private final NumberSetting timeout = number("timeout", "tt:TIMEOUT", 15, 1, 60, 1);
	private final ArrayDeque<Vec3d> points = new ArrayDeque<>();
	private long lastAdd;

	public BreadcrumbsModule() {
		super("breadcrumbs", Category.RENDER, "tt:BREADCRUMBS", "tt:BREADCRUMBS_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(BreadcrumbsModule::render);
		}
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		long now = System.currentTimeMillis();
		if (now - lastAdd > 50) {
			points.addLast(mc.player.getPos());
			lastAdd = now;
		}
		while (points.size() > timeout.getInt() * 20) {
			points.pollFirst();
		}
	}

	@Override
	protected void onDisable() {
		points.clear();
	}

	private static void render(WorldRenderContext ctx) {
		BreadcrumbsModule mod;
		try { mod = me.eldodebug.confu.ConfuClient.get().modules().get(BreadcrumbsModule.class); }
		catch (RuntimeException e) { return; }
		if (!mod.isEnabled() || mod.points.size() < 2) return;
		Vec3d cam = ctx.camera().getPos();
		Matrix4f m = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2F);
		buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		Color c = Color.WHITE;
		for (Vec3d p : mod.points) {
			buf.vertex(m, (float)(p.x-cam.x), (float)(p.y-cam.y), (float)(p.z-cam.z))
				.color(c.getRed(), c.getGreen(), c.getBlue(), 160).next();
		}
		tess.draw();
		RenderSystem.disableBlend();
	}
}
