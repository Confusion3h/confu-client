package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class EntityCullingModule extends Module {

	private static EntityCullingModule instance;
	private static final Map<UUID, OcclusionQuery> queries = new HashMap<>();
	private static Boolean supportNewGl;

	public static boolean shouldPerformCulling;

	private final NumberSetting delaySetting = number("delay", "tt:DELAY", 2, 1, 3, 1);
	private final NumberSetting distanceSetting = number("distance", "tt:DISTANCE", 45, 10, 150, 1);

	private int destroyTimer;

	public EntityCullingModule() {
		super("entity_culling", Category.OTHER, "tt:ENTITY_CULLING", "tt:ENTITY_CULLING_DESCRIPTIONN");
		instance = this;
	}

	public static EntityCullingModule get() {
		return instance;
	}

	public float distance() {
		return distanceSetting.getFloat();
	}

	private long delayMillis() {
		switch (delaySetting.getInt() - 1) {
			case 0:
				return 10L;
			case 1:
				return 25L;
			case 2:
				return 50L;
			default:
				return 0L;
		}
	}

	@Override
	public void onTick() {
		if (this.destroyTimer++ < 120) {
			return;
		}
		this.destroyTimer = 0;
		if (mc.world == null) {
			return;
		}

		Set<UUID> loaded = new HashSet<>();
		for (Entity entity : mc.world.getEntities()) {
			loaded.add(entity.getUuid());
		}

		List<UUID> remove = new ArrayList<>();
		for (OcclusionQuery query : queries.values()) {
			if (loaded.contains(query.uuid)) {
				continue;
			}
			remove.add(query.uuid);
			if (query.nextQuery != 0) {
				GL15.glDeleteQueries(query.nextQuery);
				query.nextQuery = 0;
			}
		}
		for (UUID uuid : remove) {
			queries.remove(uuid);
		}
	}

	@Override
	protected void onDisable() {
		for (OcclusionQuery query : queries.values()) {
			if (query.nextQuery != 0) {
				GL15.glDeleteQueries(query.nextQuery);
				query.nextQuery = 0;
			}
		}
		queries.clear();
	}

	public static void checkQueries() {
		EntityCullingModule mod = instance;
		if (mod == null || !mod.isEnabled()) {
			return;
		}
		long delay = mod.delayMillis();
		long now = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
		for (OcclusionQuery query : queries.values()) {
			if (query.nextQuery != 0) {
				if (GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT_AVAILABLE) != 0) {
					query.occluded = GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT) == 0;
					GL15.glDeleteQueries(query.nextQuery);
					query.nextQuery = 0;
				}
			}
			if (query.nextQuery == 0 && now - query.executionTime > delay) {
				query.executionTime = now;
				query.refresh = true;
			}
		}
	}

	public static boolean checkEntity(Entity entity, MatrixStack matrices) {
		OcclusionQuery query = queries.computeIfAbsent(entity.getUuid(), OcclusionQuery::new);

		if (query.refresh) {
			int id = GL15.glGenQueries();
			if (id == 0) {
				query.refresh = false;
				return query.occluded;
			}
			query.nextQuery = id;
			query.refresh = false;
			int mode = supportsNewGl() ? GL33.GL_ANY_SAMPLES_PASSED : GL15.GL_SAMPLES_PASSED;
			GL15.glBeginQuery(mode, id);
			Vec3d camera = mc().gameRenderer.getCamera().getPos();
			drawSelectionBoundingBox(entity.getBoundingBox()
					.expand(0.2, 0.2, 0.2)
					.offset(-camera.x, -camera.y, -camera.z), matrices);
			GL15.glEndQuery(mode);
		}

		return query.occluded;
	}

	private static void drawSelectionBoundingBox(Box box, MatrixStack matrices) {
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION);
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ);
		buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ);
		buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ);
		Tessellator.getInstance().draw();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableCull();
	}

	private static boolean supportsNewGl() {
		if (supportNewGl == null) {
			supportNewGl = GL.getCapabilities().OpenGL33;
		}
		return supportNewGl;
	}

	private static net.minecraft.client.MinecraftClient mc() {
		return net.minecraft.client.MinecraftClient.getInstance();
	}

	private static final class OcclusionQuery {

		private final UUID uuid;
		private int nextQuery;
		private boolean refresh = true;
		private boolean occluded;
		private long executionTime;

		private OcclusionQuery(UUID uuid) {
			this.uuid = uuid;
		}
	}
}
