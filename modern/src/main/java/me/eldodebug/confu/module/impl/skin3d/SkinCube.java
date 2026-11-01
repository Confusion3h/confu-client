package me.eldodebug.confu.module.impl.skin3d;

import org.joml.Matrix4f;

import net.minecraft.client.render.VertexConsumer;

/**
 * One voxel of a 3D skin layer: the 1.8.9 CustomizableCube + Polygon pair merged into one class,
 * drawn through a 1.20.1 vertex consumer instead of the tessellator.
 */
public final class SkinCube {

	private static final class Face {
		final int[] order;
		final SkinDirection direction;
		float[][] positions = new float[4][3];
		float[][] uvs = new float[4][2];

		Face(int[] order, SkinDirection direction) {
			this.order = order;
			this.direction = direction;
		}
	}

	private static final Face[] ALL_FACES = {
			new Face(new int[]{5, 4, 0, 1}, SkinDirection.DOWN),
			new Face(new int[]{2, 3, 7, 6}, SkinDirection.UP),
			new Face(new int[]{0, 4, 7, 3}, SkinDirection.WEST),
			new Face(new int[]{1, 0, 3, 2}, SkinDirection.NORTH),
			new Face(new int[]{5, 1, 2, 6}, SkinDirection.EAST),
			new Face(new int[]{4, 5, 6, 7}, SkinDirection.SOUTH)};

	private final Face[] faces;

	public SkinCube(int u, int v, float x, float y, float z, float size, SkinDirection[] hidden) {
		float px = x + size;
		float py = y + size;
		float pz = z + size;

		float[][] corners = {
				{x, y, z}, {px, y, z}, {px, py, z}, {x, py, z},
				{x, y, pz}, {px, y, pz}, {px, py, pz}, {x, py, pz}};

		// The 1.8.9 texture offsets: l=u+sizeX+sizeZ, n=l+sizeZ, q=v+sizeZ, r=q+sizeY, all over 64.
		float l = (u + size + size) / 64F;
		float n = (u + size + size + size) / 64F;
		float q = (v + size) / 64F;
		float r = (v + size + size) / 64F;

		int faceCount = 0;
		Face[] kept = new Face[6];
		for (Face template : ALL_FACES) {
			if (visible(hidden, template.direction)) {
				Face face = new Face(template.order, template.direction);
				for (int i = 0; i < 4; i++) {
					face.positions[i] = corners[template.order[i]];
				}
				face.uvs[0] = new float[]{n, q};
				face.uvs[1] = new float[]{l, q};
				face.uvs[2] = new float[]{l, r};
				face.uvs[3] = new float[]{n, r};
				kept[faceCount++] = face;
			}
		}
		faces = new Face[faceCount];
		System.arraycopy(kept, 0, faces, 0, faceCount);
	}

	private static boolean visible(SkinDirection[] hidden, SkinDirection face) {
		for (SkinDirection dir : hidden) {
			if (dir == face) {
				return false;
			}
		}
		return true;
	}

	public void render(VertexConsumer vertices, Matrix4f matrix, int light, int overlay) {
		for (Face face : faces) {
			for (int i = 0; i < 4; i++) {
				float[] pos = face.positions[i];
				float[] uv = face.uvs[i];
				vertices.vertex(matrix, pos[0], pos[1], pos[2]).color(255, 255, 255, 255)
						.texture(uv[0], uv[1]).overlay(overlay).light(light)
						.normal(face.direction.stepX, face.direction.stepY, face.direction.stepZ);
			}
		}
	}
}
