package me.eldodebug.confu.module.impl.skin3d;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/** 1.8.9 CustomizableModelPart — a positioned bundle of voxel cubes. */
public final class SkinModelPart {

	public float x;
	public float y;
	public float z;
	public boolean visible = true;

	private final List<SkinCube> cubes = new ArrayList<>();

	public void add(SkinCube cube) {
		cubes.add(cube);
	}

	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		if (!visible) {
			return;
		}
		matrices.push();
		matrices.translate(x / 16.0F, y / 16.0F, z / 16.0F);
		Matrix4f matrix = new Matrix4f(matrices.peek().getPositionMatrix());
		for (SkinCube cube : cubes) {
			cube.render(vertices, matrix, light, overlay);
		}
		matrices.pop();
	}
}
