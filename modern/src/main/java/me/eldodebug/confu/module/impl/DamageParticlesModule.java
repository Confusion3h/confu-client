package me.eldodebug.confu.module.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.soar.utils.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/** 1.8.9 DamageParticlesMod: floating damage/heal numbers above entities. */
public final class DamageParticlesModule extends Module {

	private static boolean registered;
	private static final int DAMAGE_COLOR = 0xFF55FF55;
	private static final int HEAL_COLOR = 0xFFFFFF55;

	private final Map<LivingEntity, Float> healthMap = new HashMap<>();
	private final List<DamageParticle> particles = new ArrayList<>();
	private final Random random = new Random();
	private ClientWorld world;

	public DamageParticlesModule() {
		super("damage_particles", Category.RENDER, "tt:DAMAGE_PARTICLES", "tt:DAMAGE_PARTICLES_DESCRIPTION");
		if (!registered) {
			registered = true;
			WorldRenderEvents.AFTER_TRANSLUCENT.register(DamageParticlesModule::render);
		}
	}

	@Override
	public void onTick() {
		if (mc.world != world) {
			world = mc.world;
			healthMap.clear();
			particles.clear();
		}
		if (mc.world == null) {
			return;
		}

		Iterator<DamageParticle> iterator = particles.iterator();
		while (iterator.hasNext()) {
			DamageParticle particle = iterator.next();
			particle.ticks++;
			if (particle.ticks <= 10) {
				particle.y += particle.ticks * 0.005;
			}
			if (particle.ticks > 20) {
				iterator.remove();
			}
		}

		healthMap.keySet().removeIf(entity -> entity.isRemoved() || entity.getWorld() != mc.world);

		for (Entity entity : mc.world.getEntities()) {
			if (!(entity instanceof LivingEntity living) || living == mc.player) {
				continue;
			}
			float after = living.getHealth();
			Float before = healthMap.get(living);
			if (before == null) {
				healthMap.put(living, after);
				continue;
			}
			if (before != after) {
				boolean damaged = after < before;
				double amount = MathUtils.roundToPlace(damaged ? before - after : after - before, 1);
				double x = living.getX() - 0.5 + random.nextInt(5) * 0.1;
				double y = living.getBoundingBox().minY + living.getBoundingBox().getYLength() / 2;
				double z = living.getZ() - 0.5 + random.nextInt(5) * 0.1;
				particles.add(new DamageParticle(x, y, z, String.valueOf(amount), damaged ? DAMAGE_COLOR : HEAL_COLOR));
				healthMap.put(living, after);
			}
		}
	}

	private static void render(WorldRenderContext ctx) {
		DamageParticlesModule mod;
		try {
			mod = me.eldodebug.confu.ConfuClient.get().modules().get(DamageParticlesModule.class);
		} catch (RuntimeException e) {
			return;
		}
		if (!mod.isEnabled() || mod.particles.isEmpty() || mod.mc.world == null) {
			return;
		}
		Vec3d cam = ctx.camera().getPos();
		float yaw = ctx.camera().getYaw();
		float pitch = ctx.camera().getPitch();
		boolean front = mod.mc.options.getPerspective().isFrontView();
		MatrixStack matrices = ctx.matrixStack();
		VertexConsumerProvider.Immediate consumers = mod.mc.getBufferBuilders().getEntityVertexConsumers();
		TextRenderer font = mod.mc.textRenderer;
		for (DamageParticle particle : mod.particles) {
			matrices.push();
			matrices.translate(particle.x - cam.x, particle.y - cam.y, particle.z - cam.z);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(front ? -pitch : pitch));
			matrices.scale(-0.03F, -0.03F, 0.03F);
			float width = font.getWidth(particle.text);
			font.draw(particle.text, -width / 2F, -(font.fontHeight - 1), particle.color, true,
					matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
			matrices.pop();
		}
		consumers.draw();
	}

	private static final class DamageParticle {

		private final double x;
		private double y;
		private final double z;
		private final String text;
		private final int color;
		private int ticks;

		private DamageParticle(double x, double y, double z, String text, int color) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.text = text;
			this.color = color;
		}
	}
}
