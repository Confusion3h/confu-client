package me.eldodebug.confu.module.impl.skin3d;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.texture.NativeImage;

/** 1.8.9 SolidPixelWrapper — turns every opaque skin pixel of a box shell into a voxel cube. */
public final class SolidPixelWrapper {

	private SolidPixelWrapper() {
	}

	public static SkinModelPart wrapBox(NativeImage skin, int width, int height, int depth, int textureU, int textureV,
			boolean topPivot, float rotationOffset) {

		SkinModelPart part = new SkinModelPart();
		float pixelSize = 1f;
		float staticXOffset = -width / 2f;
		float staticYOffset = topPivot ? +rotationOffset : -height + rotationOffset;
		float staticZOffset = -depth / 2f;

		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
						textureU + depth + u, textureV + depth + v, staticXOffset + u, staticYOffset + v, staticZOffset,
						SkinDirection.SOUTH);

				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
						textureU + 2 * depth + width + u, textureV + depth + v, staticXOffset + width - 1 - u,
						staticYOffset + v, staticZOffset + depth - 1, SkinDirection.NORTH);
			}
		}

		for (int u = 0; u < depth; u++) {
			for (int v = 0; v < height; v++) {
				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
						textureU - 1 + depth - u, textureV + depth + v, staticXOffset, staticYOffset + v,
						staticZOffset + u, SkinDirection.EAST);

				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
						textureU + depth + width + u, textureV + depth + v, staticXOffset + width - 1f,
						staticYOffset + v, staticZOffset + u, SkinDirection.WEST);
			}
		}

		for (int u = 0; u < width; u++) {
			for (int v = 0; v < depth; v++) {
				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
						textureU + depth + u, textureV + depth - 1 - v, staticXOffset + u, staticYOffset,
						staticZOffset + v, SkinDirection.UP);

				addPixel(skin, part, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
						textureU + depth + width + u, textureV + depth - 1 - v, staticXOffset + u,
						staticYOffset + height - 1f, staticZOffset + v, SkinDirection.DOWN);
			}
		}

		return part;
	}

	private static final int[][] OFFSETS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
	private static final java.util.Map<SkinDirection, SkinDirection[]> HIDDEN = java.util.Map.of(
			SkinDirection.NORTH, new SkinDirection[]{SkinDirection.WEST, SkinDirection.EAST, SkinDirection.UP, SkinDirection.DOWN},
			SkinDirection.SOUTH, new SkinDirection[]{SkinDirection.EAST, SkinDirection.WEST, SkinDirection.UP, SkinDirection.DOWN},
			SkinDirection.WEST, new SkinDirection[]{SkinDirection.SOUTH, SkinDirection.NORTH, SkinDirection.UP, SkinDirection.DOWN},
			SkinDirection.EAST, new SkinDirection[]{SkinDirection.NORTH, SkinDirection.SOUTH, SkinDirection.UP, SkinDirection.DOWN},
			SkinDirection.UP, new SkinDirection[]{SkinDirection.EAST, SkinDirection.WEST, SkinDirection.NORTH, SkinDirection.SOUTH},
			SkinDirection.DOWN, new SkinDirection[]{SkinDirection.EAST, SkinDirection.WEST, SkinDirection.NORTH, SkinDirection.SOUTH});

	private static void addPixel(NativeImage skin, SkinModelPart part, float pixelSize, boolean onBorder, int u, int v,
			float x, float y, float z, SkinDirection dir) {

		if (alpha(skin, u, v) == 0) {
			return;
		}

		Set<SkinDirection> hide = EnumSet.noneOf(SkinDirection.class);
		if (!onBorder) {
			for (int i = 0; i < OFFSETS.length; i++) {
				int tU = u + OFFSETS[i][1];
				int tV = v + OFFSETS[i][0];
				if (tU >= 0 && tU < 64 && tV >= 0 && tV < 64 && alpha(skin, tU, tV) != 0) {
					hide.add(HIDDEN.get(dir)[i]);
				}
			}
			hide.add(dir);
		}

		part.add(new SkinCube(u - 2, v - 1, x, y, z, pixelSize, hide.toArray(new SkinDirection[0])));
	}

	private static int alpha(NativeImage skin, int u, int v) {
		if (u < 0 || v < 0 || u >= skin.getWidth() || v >= skin.getHeight()) {
			return 0;
		}
		return skin.getColor(u, v) >>> 24;
	}
}
