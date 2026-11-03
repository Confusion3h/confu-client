package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ColorSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.HitboxRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** 1.8.9 HitBoxMod: entity hitboxes with custom color, alpha, parts and line width. */
public final class HitBoxModule extends Module {

	private final ColorSetting color = color("color", "tt:COLOR", 0xFFFFFFFF, false);
	private final NumberSetting alpha = number("alpha", "tt:ALPHA", 1, 0, 1, 0.01);
	private final BooleanSetting boundingBox = bool("bounding_box", "tt:BOUNDING_BOX", true);
	private final BooleanSetting eyeHeight = bool("eye_height", "tt:EYE_HEIGHT", true);
	private final BooleanSetting lookVector = bool("look_vector", "tt:LOOK_VECTOR", true);
	private final NumberSetting lineWidth = number("line_width", "tt:LINE_WIDTH", 2, 1, 5, 1);

	public HitBoxModule() {
		super("hitbox", Category.RENDER, "tt:HITBOX", "tt:HITBOX_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		if (mc.getEntityRenderDispatcher() != null) mc.getEntityRenderDispatcher().setRenderHitboxes(true);
	}

	@Override
	public void onTick() {
		if (mc.getEntityRenderDispatcher() != null && !mc.getEntityRenderDispatcher().shouldRenderHitboxes()) {
			mc.getEntityRenderDispatcher().setRenderHitboxes(true);
		}
	}

	@Override
	protected void onDisable() {
		if (mc.getEntityRenderDispatcher() != null) mc.getEntityRenderDispatcher().setRenderHitboxes(false);
	}

	/** Replaces vanilla renderHitbox; matrices are already at the entity's render position. Port of 1.8.9 onRenderHitbox. */
	public void render(MatrixStack matrices, VertexConsumerProvider provider, Entity entity, float tickDelta) {
		if (entity instanceof ArmorStandEntity) {
			return;
		}
		VertexConsumer lines = provider.getBuffer(HitboxRenderLayers.lines(lineWidth.getFloat()));
		float a = alpha.getFloat();
		float half = entity.getWidth() / 2.0F;

		if (boundingBox.enabled()) {
			int c = color.argb();
			Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
			WorldRenderer.drawBox(matrices, lines, box, ((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F, a);
		}

		if (eyeHeight.enabled() && entity instanceof LivingEntity) {
			float eye = entity.getStandingEyeHeight();
			WorldRenderer.drawBox(matrices, lines, -half, eye - 0.01F, -half, half, eye + 0.01F, half, 1F, 0F, 0F, a);
		}

		if (lookVector.enabled()) {
			Vec3d look = entity.getRotationVec(tickDelta);
			float eye = entity.getStandingEyeHeight();
			Matrix4f pos = matrices.peek().getPositionMatrix();
			Matrix3f normal = matrices.peek().getNormalMatrix();
			float nx = (float) look.x, ny = (float) look.y, nz = (float) look.z;
			lines.vertex(pos, 0F, eye, 0F).color(0, 0, 255, 255).normal(normal, nx, ny, nz).next();
			lines.vertex(pos, nx * 2F, eye + ny * 2F, nz * 2F).color(0, 0, 255, (int) (a * 255)).normal(normal, nx, ny, nz).next();
		}
	}
}
