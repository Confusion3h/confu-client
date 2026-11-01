package me.eldodebug.confu.module.impl.minimap;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkAtlas implements Iterable<ChunkTile> {

	private final ChunkPos[] chunkCoords;
	private final BitSet reusableChunks;
	private final int radius;
	private final int chunkSpanL2;
	private final double chunkWidth, chunkHeight;
	private final IntBuffer pixels;
	private final int texture;

	public ChunkAtlas(int maxChunkRadius) {

		int maxChunks = maxChunkRadius * maxChunkRadius << 2;

		int texWidth = Integer.highestOneBit(maxChunks - 1) << 5;
		int texHeight = 16;

		int texLimit = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
		while (texWidth > texLimit) {
			texWidth >>= 1;
			texHeight <<= 1;
		}

		while (texHeight > texLimit) {
			texHeight >>= 1;
		}

		int chunkCapacity = texWidth * texHeight >> 8;

		if (maxChunks > chunkCapacity) {
			maxChunks = chunkCapacity;
		}

		this.radius = (int) Math.sqrt(maxChunks >> 2);
		this.chunkSpanL2 = Integer.numberOfTrailingZeros(texWidth >> 4);

		this.chunkWidth = 16.0 / texWidth;
		this.chunkHeight = 16.0 / texHeight;

		this.chunkCoords = new ChunkPos[maxChunks];
		this.reusableChunks = new BitSet(maxChunks);

		this.texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(
				GL11.GL_TEXTURE_2D, 0,
				GL11.GL_RGBA,
				texWidth, texHeight, 0,
				GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
				(IntBuffer) null
		);

		this.pixels = BufferUtils.createIntBuffer(256);
	}

	public int getChunkRadius() {
		return this.radius;
	}

	public int getTextureHandle() {
		return this.texture;
	}

	public double getSpriteX(int offset) {
		return (offset & ((1 << this.chunkSpanL2) - 1)) * this.chunkWidth;
	}

	public double getSpriteY(int offset) {
		return (offset >> this.chunkSpanL2) * this.chunkHeight;
	}

	public double getSpriteWidth() {
		return this.chunkWidth;
	}

	public double getSpriteHeight() {
		return this.chunkHeight;
	}

	public void clear() {
		Arrays.fill(this.chunkCoords, null);
	}

	public void loadChunks(int chunkX, int chunkZ) {
		ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null) {
			return;
		}

		this.reusableChunks.clear();

		for (int offs = 0; offs < this.chunkCoords.length; offs++) {
			ChunkPos coords = this.chunkCoords[offs];
			if (coords == null) {
				continue;
			}

			int offsX = coords.x - chunkX;
			int offsZ = coords.z - chunkZ;

			if (offsX < -this.radius || this.radius <= offsX) {
				this.chunkCoords[offs] = null;
				continue;
			}

			if (offsZ < -this.radius || this.radius <= offsZ) {
				this.chunkCoords[offs] = null;
				continue;
			}

			offsX += this.radius;
			offsZ += this.radius;

			this.reusableChunks.set(offsX + offsZ * this.radius * 2);
		}

		for (int relZ = this.radius * 2 - 1; relZ >= 0; relZ--) {
			for (int relX = this.radius * 2 - 1; relX >= 0; relX--) {
				int checkIdx = relX + relZ * this.radius * 2;

				if (this.reusableChunks.get(checkIdx)) {
					continue;
				}

				int x = chunkX + relX - this.radius;
				int z = chunkZ + relZ - this.radius;

				WorldChunk c = this.getLoadedChunk(x, z);
				if (c == null) {
					continue;
				}

				this.reserveOffset(c);

				this.recolorChunk(x, z + 1);
			}
		}
	}

	public void refreshChunk(int x, int z) {
		this.recolorChunk(x, z);
		this.recolorChunk(x, z + 1);
	}

	@Override
	public Iterator<ChunkTile> iterator() {
		List<ChunkTile> tiles = new ArrayList<>();
		for (int offs = 0; offs < this.chunkCoords.length; offs++) {
			ChunkPos coords = this.chunkCoords[offs];
			if (coords != null) {
				tiles.add(new ChunkTile(coords.x, coords.z, offs));
			}
		}
		return tiles.iterator();
	}

	private void reserveOffset(WorldChunk c) {

		int offs = this.searchChunkAtlas(null);

		if (offs == -1) {
			throw new IllegalStateException("Chunk coordinate array full.");
		}

		this.chunkCoords[offs] = c.getPos();

		this.updateColorData(c, offs);
	}

	private void recolorChunk(int x, int z) {

		WorldChunk c = this.getLoadedChunk(x, z);

		if (c == null) {
			return;
		}

		int offs = this.searchChunkAtlas(new ChunkPos(x, z));
		if (offs == -1) {
			return;
		}

		this.updateColorData(c, offs);
	}

	private void updateColorData(WorldChunk src, int offs) {

		this.computeColors(src);

		int x = offs & ((1 << this.chunkSpanL2) - 1);
		int y = offs >> this.chunkSpanL2;

		x <<= 4;
		y <<= 4;

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
		GL11.glTexSubImage2D(
				GL11.GL_TEXTURE_2D, 0,
				x, y, 16, 16,
				GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
				this.pixels
		);
	}

	private void computeColors(WorldChunk src) {

		ClientWorld world = MinecraftClient.getInstance().world;
		int[] northHeights = new int[16];

		Arrays.fill(northHeights, -1);

		WorldChunk north = this.getLoadedChunk(src.getPos().x, src.getPos().z - 1);
		if (north != null) {
			for (int x = 0; x < 16; x++) {
				northHeights[x] = this.getTopColoredBlockState(north, x, 15, world).getY();
			}
		}

		for (int x = 0; x < 16; x++) {
			int northHeight = northHeights[x];

			for (int z = 0; z < 16; z++) {
				BlockPos pos = this.getTopColoredBlockState(src, x, z, world);
				BlockState state = src.getBlockState(pos);
				MapColor color = state.getMapColor(world, pos);

				int height = pos.getY();
				int shade = 1;

				if (northHeight > height) {
					shade = 0;
				} else if (northHeight >= 0 && northHeight < height) {
					shade = 2;
				}

				int depth = 0;
				BlockPos.Mutable depthPos = pos.mutableCopy();
				while (depthPos.getY() >= 0 && !state.isSolid()) {
					depthPos.setY(depthPos.getY() - 1);
					state = src.getBlockState(depthPos);
					depth++;
				}

				if (depth > 0) {
					int dither = depth + (((x ^ z) & 1) << 1);

					if (dither < 5) {
						shade = 2;
					} else if (dither > 9) {
						shade = 0;
					}
				}

				int rgb;

				if (height > 0) {
					MapColor.Brightness brightness = shade == 0 ? MapColor.Brightness.LOW
							: shade == 2 ? MapColor.Brightness.HIGH : MapColor.Brightness.NORMAL;
					rgb = color.getRenderColor(brightness) | 0xFF000000;
				} else if (((x ^ z) & 3) == 0) {
					rgb = 0xFF2D2D5A;
				} else {
					rgb = 0xFF1E1E3C;
				}

				northHeight = height;

				this.pixels.put(x | z << 4, rgb);
			}
		}
	}

	private BlockPos getTopColoredBlockState(WorldChunk src, int localX, int localZ, ClientWorld world) {

		ChunkPos chunkPos = src.getPos();
		int worldX = chunkPos.getStartX() + localX;
		int worldZ = chunkPos.getStartZ() + localZ;
		int y = src.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(localX, localZ) - 1;

		BlockPos.Mutable pos = new BlockPos.Mutable(worldX, y, worldZ);

		while (y >= src.getBottomY()) {
			pos.set(worldX, y, worldZ);
			BlockState state = src.getBlockState(pos);

			if (state.getMapColor(world, pos) != MapColor.CLEAR) {
				break;
			}

			y--;
		}

		return pos.set(worldX, y, worldZ);
	}

	private int searchChunkAtlas(ChunkPos c) {

		for (int offs = 0; offs < this.chunkCoords.length; offs++) {
			if (Objects.equals(c, this.chunkCoords[offs])) {
				return offs;
			}
		}

		return -1;
	}

	private WorldChunk getLoadedChunk(int x, int z) {

		ClientWorld world = MinecraftClient.getInstance().world;

		if (world == null) {
			return null;
		}

		ClientChunkManager manager = world.getChunkManager();
		if (!manager.isChunkLoaded(x, z)) {
			return null;
		}

		WorldChunk c = manager.getWorldChunk(x, z, false);
		if (c == null || c.isEmpty()) {
			return null;
		}

		return c;
	}
}
