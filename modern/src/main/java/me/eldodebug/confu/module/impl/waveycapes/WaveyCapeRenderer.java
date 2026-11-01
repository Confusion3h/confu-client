package me.eldodebug.confu.module.impl.waveycapes;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import me.eldodebug.confu.module.impl.WaveyCapesModule;
import me.eldodebug.soar.utils.MathUtils;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** 1.8.9 CustomCapeRenderLayer + SmoothCapeRenderer drawn with 1.20.1 vertex consumers. */
public final class WaveyCapeRenderer {

	public static final int PART_COUNT = 16;

	private static ModelPart[] strips;

	private WaveyCapeRenderer() {
	}

	public static void render(MatrixStack matrices, VertexConsumerProvider providers, int light,
			AbstractClientPlayerEntity player, float tickDelta, WaveyCapeSimulation holder) {

		WaveyCapesModule module = WaveyCapesModule.get();
		if (module == null || player.getCapeTexture() == null) {
			return;
		}

		StickSimulation simulation = holder.simulation();
		if (module.basicMovement()) {
			holder.updateSimulation(player, PART_COUNT);
		}

		VertexConsumer vertices = providers.getBuffer(RenderLayer.getEntitySolid(player.getCapeTexture()));

		if (module.smoothStyle()) {
			renderSmooth(vertices, light, player, tickDelta, simulation, module);
		} else {
			renderBlocky(matrices, vertices, light, player, tickDelta, simulation, module);
		}
	}

	// --- blocky: 16 strips with the 1.8.9 ModelRenderer geometry (64x32 layout, row i) ---

	private static void renderBlocky(MatrixStack matrices, VertexConsumer vertices, int light,
			AbstractClientPlayerEntity player, float tickDelta, StickSimulation simulation, WaveyCapesModule module) {

		ModelPart[] parts = strips();
		for (int part = 0; part < PART_COUNT; part++) {
			matrices.push();
			if (module.basicMovement()) {
				poseSimulationBlocky(matrices, player, tickDelta, simulation, part);
			} else {
				poseVanillaBlocky(matrices, player, tickDelta, part);
			}
			parts[part].render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
			matrices.pop();
		}
	}

	private static ModelPart[] strips() {
		if (strips == null) {
			strips = new ModelPart[PART_COUNT];
			for (int i = 0; i < PART_COUNT; i++) {
				final int row = i;
				strips[i] = new ModelPart(List.of(new ModelPart.Cuboid(0, row, -5.0F, (float) row, -1.0F,
						10.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, false, 64.0F, 32.0F, EnumSet.allOf(Direction.class))), Map.of());
			}
		}
		return strips;
	}

	// --- smooth: quads chained between neighbouring strip frames, 1.8.9 UV mapping ---

	private static void renderSmooth(VertexConsumer vertices, int light, AbstractClientPlayerEntity player,
			float tickDelta, StickSimulation simulation, WaveyCapesModule module) {

		MatrixStack pose = new MatrixStack();
		Matrix4f oldPositionMatrix = null;

		for (int part = 0; part < PART_COUNT; part++) {
			if (module.basicMovement()) {
				pose.push();
				poseSimulationSmooth(pose, player, tickDelta, simulation, part);
			} else {
				pose.push();
				poseVanillaSmooth(pose, player, tickDelta, part);
			}

			Matrix4f positionMatrix = new Matrix4f(pose.peek().getPositionMatrix());
			if (oldPositionMatrix == null) {
				oldPositionMatrix = new Matrix4f(positionMatrix);
			}

			if (part == 0) {
				addTop(vertices, light, oldPositionMatrix, positionMatrix, part);
			}
			if (part == PART_COUNT - 1) {
				addBottom(vertices, light, positionMatrix, positionMatrix, part);
			}
			addLeft(vertices, light, positionMatrix, oldPositionMatrix, part);
			addRight(vertices, light, positionMatrix, oldPositionMatrix, part);
			addBack(vertices, light, positionMatrix, oldPositionMatrix, part);
			addFront(vertices, light, oldPositionMatrix, positionMatrix, part);

			oldPositionMatrix = positionMatrix;
			pose.pop();
		}
	}

	private static float v0(int part) {
		return part * (0.96F / PART_COUNT);
	}

	private static float v1(int part) {
		return (part + 1) * (0.96F / PART_COUNT);
	}

	private static float minV(int part) {
		float minV = 0.03125F;
		return minV + ((0.53125F - 0.03125F) / PART_COUNT) * part;
	}

	private static float maxV(int part) {
		float minV = 0.03125F;
		return minV + ((0.53125F - 0.03125F) / PART_COUNT) * (part + 1);
	}

	private static void vertex(VertexConsumer v, Matrix4f matrix, float x, float y, float z, float u, float v0,
			float nx, float ny, float nz, int light) {
		v.vertex(matrix, x, y, z).color(255, 255, 255, 255).texture(u, v0).overlay(OverlayTexture.DEFAULT_UV)
				.light(light).normal(nx, ny, nz);
	}

	// Coordinates below are the literals from the 1.8.9 add*Vertex calls (x1>=x2, y1>=y2 there, so no swaps apply).
	private static void addBack(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0.015625F;
		float maxU = 0.171875F;
		float u = maxU;
		float vv = minV(part);
		vertex(v, oldMatrix, 0.3F, v0(part), -0.06F, u, vv, 1, 0, 0, light);
		vertex(v, oldMatrix, -0.3F, v0(part), -0.06F, minU, vv, 1, 0, 0, light);
		vertex(v, matrix, -0.3F, v1(part), -0.06F, minU, maxV(part), 1, 0, 0, light);
		vertex(v, matrix, 0.3F, v1(part), -0.06F, maxU, maxV(part), 1, 0, 0, light);
	}

	private static void addFront(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0.1875F;
		float maxU = 0.34375F;
		vertex(v, oldMatrix, 0.3F, v1(part), 0F, maxU, maxV(part), 1, 0, 0, light);
		vertex(v, oldMatrix, -0.3F, v1(part), 0F, minU, maxV(part), 1, 0, 0, light);
		vertex(v, matrix, -0.3F, v0(part), 0F, minU, minV(part), 1, 0, 0, light);
		vertex(v, matrix, 0.3F, v0(part), 0F, maxU, minV(part), 1, 0, 0, light);
	}

	private static void addLeft(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0F;
		float maxU = 0.015625F;
		vertex(v, matrix, -0.3F, v1(part), 0F, maxU, maxV(part), 1, 0, 0, light);
		vertex(v, matrix, -0.3F, v1(part), -0.06F, minU, maxV(part), 1, 0, 0, light);
		vertex(v, oldMatrix, -0.3F, v0(part), -0.06F, minU, minV(part), 1, 0, 0, light);
		vertex(v, oldMatrix, -0.3F, v0(part), 0F, maxU, minV(part), 1, 0, 0, light);
	}

	private static void addRight(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0.171875F;
		float maxU = 0.1875F;
		vertex(v, matrix, 0.3F, v1(part), -0.06F, minU, maxV(part), 1, 0, 0, light);
		vertex(v, matrix, 0.3F, v1(part), 0F, maxU, maxV(part), 1, 0, 0, light);
		vertex(v, oldMatrix, 0.3F, v0(part), 0F, maxU, minV(part), 1, 0, 0, light);
		vertex(v, oldMatrix, 0.3F, v0(part), -0.06F, minU, minV(part), 1, 0, 0, light);
	}

	private static void addBottom(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0.171875F;
		float maxU = 0.328125F;
		float minV = 0F;
		float maxV = 0.03125F;
		float y = v1(part);
		vertex(v, oldMatrix, 0.3F, y, -0.06F, maxU, minV, 1, 0, 0, light);
		vertex(v, oldMatrix, -0.3F, y, -0.06F, minU, minV, 1, 0, 0, light);
		vertex(v, matrix, -0.3F, y, 0F, minU, maxV, 1, 0, 0, light);
		vertex(v, matrix, 0.3F, y, 0F, maxU, maxV, 1, 0, 0, light);
	}

	private static void addTop(VertexConsumer v, int light, Matrix4f matrix, Matrix4f oldMatrix, int part) {
		float minU = 0.015625F;
		float maxU = 0.171875F;
		float minV = 0F;
		float maxV = 0.03125F;
		vertex(v, matrix, 0.3F, 0F, 0F, maxU, maxV, 0, 1, 0, light);
		vertex(v, matrix, -0.3F, 0F, 0F, minU, maxV, 0, 1, 0, light);
		vertex(v, oldMatrix, -0.3F, 0F, -0.06F, minU, minV, 0, 1, 0, light);
		vertex(v, oldMatrix, 0.3F, 0F, -0.06F, maxU, minV, 0, 1, 0, light);
	}

	// --- pose math, ported line by line from the 1.8.9 layer ---

	private static void poseSimulationSmooth(MatrixStack pose, AbstractClientPlayerEntity player, float delta,
			StickSimulation simulation, int part) {

		pose.translate(0.0D, 0.0D, 0.125D);

		float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
		if (z > 0) {
			z = 0;
		}
		float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);

		float partRotation = (float) -Math.atan2(y, z);
		partRotation = Math.max(partRotation, 0);
		if (partRotation != 0) {
			partRotation = (float) (Math.PI - partRotation);
		}
		partRotation *= 57.2958;
		partRotation *= 2;

		float sidewaysRotationOffset = 0;
		float height = 0;

		if (player.isInSneakingPose()) {
			height += 25.0F;
			pose.translate(0, 0.15F, 0);
		}

		pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + height + naturalWindSwing(part)));
		pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sidewaysRotationOffset / 2.0F));
		pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
		pose.translate(0, y / PART_COUNT, z / PART_COUNT);
		pose.translate(0, 0.48 / 16, -(0.48 / 16));
		pose.translate(0, part * 1f / PART_COUNT, 0);
		pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-partRotation));
		pose.translate(0, -part * 1f / PART_COUNT, 0);
		pose.translate(0, -(0.48 / 16), 0.48 / 16);
	}

	private static void poseVanillaSmooth(MatrixStack pose, AbstractClientPlayerEntity player, float tickDelta, int part) {

		pose.translate(0.0D, 0.0D, 0.125D);
		double d = MathUtils.lerp(tickDelta, player.prevCapeX, player.capeX) - MathUtils.lerp(tickDelta, player.prevX, player.getX());
		double e = MathUtils.lerp(tickDelta, player.prevCapeY, player.capeY) - MathUtils.lerp(tickDelta, player.prevY, player.getY());
		double m = MathUtils.lerp(tickDelta, player.prevCapeZ, player.capeZ) - MathUtils.lerp(tickDelta, player.prevZ, player.getZ());
		float n = player.prevBodyYaw + player.bodyYaw - player.prevBodyYaw;
		double o = Math.sin(n * 0.017453292F);
		double p = -Math.cos(n * 0.017453292F);
		float height = (float) e * 10.0F;
		height = MathHelper.clamp(height, -6.0F, 32.0F);
		float swing = (float) (d * o + m * p) * easeOutSine(1.0F / PART_COUNT * part) * 100.0F;
		swing = MathHelper.clamp(swing, 0.0F, 150.0F * easeOutSine(1.0F / PART_COUNT * part));
		float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
		sidewaysRotationOffset = MathHelper.clamp(sidewaysRotationOffset, -20.0F, 20.0F);
		height += Math.sin(MathUtils.lerp(tickDelta, player.prevHorizontalSpeed, player.horizontalSpeed) * 6.0F) * 32.0F
				* MathUtils.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);

		if (player.isInSneakingPose()) {
			height += 25.0F;
			pose.translate(0, 0.15F, 0);
		}

		pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + swing / 2.0F + height + naturalWindSwing(part)));
		pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sidewaysRotationOffset / 2.0F));
		pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
	}

	private static void poseSimulationBlocky(MatrixStack matrices, AbstractClientPlayerEntity player, float delta,
			StickSimulation simulation, int part) {

		matrices.translate(0.0D, 0.0D, 0.125D);

		float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
		if (z > 0) {
			z = 0;
		}
		float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);

		float height = 0;
		if (player.isInSneakingPose()) {
			height += 25.0F;
			matrices.translate(0, 0.15F, 0);
		}

		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + height + naturalWindSwing(part)));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		matrices.translate(0, y / PART_COUNT, z / PART_COUNT);
		matrices.translate(0, 0.48 / 16, -(0.48 / 16));
		matrices.translate(0, part * 1f / PART_COUNT, 0);
		matrices.translate(0, -part * 1f / PART_COUNT, 0);
		matrices.translate(0, -(0.48 / 16), 0.48 / 16);
	}

	private static void poseVanillaBlocky(MatrixStack matrices, AbstractClientPlayerEntity player, float tickDelta, int part) {

		matrices.translate(0.0D, 0.0D, 0.125D);
		double d = MathUtils.lerp(tickDelta, player.prevCapeX, player.capeX) - MathUtils.lerp(tickDelta, player.prevX, player.getX());
		double e = MathUtils.lerp(tickDelta, player.prevCapeY, player.capeY) - MathUtils.lerp(tickDelta, player.prevY, player.getY());
		double m = MathUtils.lerp(tickDelta, player.prevCapeZ, player.capeZ) - MathUtils.lerp(tickDelta, player.prevZ, player.getZ());
		float n = player.prevBodyYaw + player.bodyYaw - player.prevBodyYaw;
		double o = Math.sin(n * 0.017453292F);
		double p = -Math.cos(n * 0.017453292F);
		float height = (float) e * 10.0F;
		height = MathHelper.clamp(height, -6.0F, 32.0F);
		float swing = (float) (d * o + m * p) * easeOutSine(1.0F / PART_COUNT * part) * 100.0F;
		swing = MathHelper.clamp(swing, 0.0F, 150.0F * easeOutSine(1.0F / PART_COUNT * part));
		float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
		sidewaysRotationOffset = MathHelper.clamp(sidewaysRotationOffset, -20.0F, 20.0F);
		height += Math.sin(MathUtils.lerp(tickDelta, player.prevHorizontalSpeed, player.horizontalSpeed) * 6.0F) * 32.0F
				* MathUtils.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);

		if (player.isInSneakingPose()) {
			height += 25.0F;
			matrices.translate(0, 0.15F, 0);
		}

		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + swing / 2.0F + height + naturalWindSwing(part)));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sidewaysRotationOffset / 2.0F));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
	}

	private static float naturalWindSwing(int part) {
		WaveyCapesModule module = WaveyCapesModule.get();
		if (module != null && module.waves()) {
			long highlightedPart = (System.currentTimeMillis() / 3) % 360;
			float relativePart = (float) (part + 1) / PART_COUNT;
			return (float) (Math.sin(Math.toRadians(relativePart * 360 - highlightedPart)) * 3);
		}
		return 0;
	}

	private static float easeOutSine(float x) {
		return (float) Math.sin((x * Math.PI) / 2f);
	}
}
