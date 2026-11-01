package me.eldodebug.confu.module.impl.skin3d;

/** Face of the skin cube, ported from the 1.8.9 SkinDirection. */
public enum SkinDirection {

	DOWN(0, -1, 0), UP(0, 1, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1), WEST(-1, 0, 0), EAST(1, 0, 0);

	public final int stepX;
	public final int stepY;
	public final int stepZ;

	SkinDirection(int stepX, int stepY, int stepZ) {
		this.stepX = stepX;
		this.stepY = stepY;
		this.stepZ = stepZ;
	}
}
