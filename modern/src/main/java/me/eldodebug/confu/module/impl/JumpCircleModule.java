package me.eldodebug.confu.module.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;

/** 1.8.9 JumpCircleMod. */
public final class JumpCircleModule extends Module {

	private static boolean registered;
	private final List<Circle> circles = new ArrayList<>();
	private boolean wasOnGround = true;

	public JumpCircleModule() {
		super("jump_circle", Category.RENDER, "tt:JUMP_CIRCLE", "tt:JUMP_CIRCLE_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(JumpCircleModule::render);
		}
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		boolean onGround = mc.player.isOnGround();
		if (wasOnGround && !onGround) {
			circles.add(new Circle(mc.player.getPos()));
		}
		wasOnGround = onGround;
		circles.removeIf(c -> System.currentTimeMillis() - c.born > 1000);
	}

	private static void render(WorldRenderContext ctx) {
		JumpCircleModule mod;
		try { mod = me.eldodebug.confu.ConfuClient.get().modules().get(JumpCircleModule.class); }
		catch (RuntimeException e) { return; }
		if (!mod.isEnabled() || mod.circles.isEmpty()) return;
		Vec3d cam = ctx.camera().getPos();
		Matrix4f m = ctx.matrixStack().peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2F);
		for (Circle c : mod.circles) {
			float t = (System.currentTimeMillis() - c.born) / 1000F;
			float r = 0.3F + t * 1.2F;
			int a = (int) ((1F - t) * 200);
			buf.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
			for (int i = 0; i <= 48; i++) {
				double ang = i / 48.0 * Math.PI * 2;
				buf.vertex(m, (float)(c.pos.x - cam.x + Math.cos(ang)*r), (float)(c.pos.y - cam.y + 0.05),
						(float)(c.pos.z - cam.z + Math.sin(ang)*r)).color(255, 255, 255, a).next();
			}
			tess.draw();
		}
		RenderSystem.disableBlend();
	}

	private static final class Circle {
		final Vec3d pos;
		final long born = System.currentTimeMillis();
		Circle(Vec3d pos) { this.pos = pos; }
	}
}
