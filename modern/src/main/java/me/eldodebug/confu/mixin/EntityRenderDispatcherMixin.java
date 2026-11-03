package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.EntityCullingModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	private static VertexConsumerProvider confu$hitboxProvider;

	@Inject(method = "render", at = @At("HEAD"))
	private void confu$captureProvider(Entity entity, double x, double y, double z, float yaw, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		confu$hitboxProvider = vertexConsumers;
	}

	@Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
	private static void confu$hitbox(MatrixStack matrices, net.minecraft.client.render.VertexConsumer vertices, Entity entity, float tickDelta, CallbackInfo ci) {
		me.eldodebug.confu.module.impl.HitBoxModule mod = me.eldodebug.confu.ConfuClient.get().modules().get(me.eldodebug.confu.module.impl.HitBoxModule.class);
		if (mod != null && mod.isEnabled() && confu$hitboxProvider != null) {
			ci.cancel();
			mod.render(matrices, confu$hitboxProvider, entity, tickDelta);
		}
	}


	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void confu$cullEntity(Entity entity, double x, double y, double z, float yaw, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!EntityCullingModule.shouldPerformCulling) {
			return;
		}

		EntityCullingModule mod = EntityCullingModule.get();
		if (mod == null || !mod.isEnabled()) {
			return;
		}

		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || !(entity instanceof LivingEntity)) {
			return;
		}

		if (entity == mc.player || entity.getWorld() != mc.player.getWorld()) {
			return;
		}

		boolean armorStand = entity instanceof ArmorStandEntity;

		if (armorStand && ((ArmorStandEntity) entity).isMarker()) {
			return;
		}

		if (entity.isInvisibleTo(mc.player)) {
			return;
		}

		if (EntityCullingModule.checkEntity(entity, matrices)) {
			ci.cancel();

			EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
			EntityRenderer<?> renderer = dispatcher.getRenderer(entity);
			EntityRendererAccessor accessor = (EntityRendererAccessor) renderer;

			if (accessor.confu$hasLabel(entity)) {
				matrices.push();
				matrices.translate(x, y, z);
				accessor.confu$renderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
				matrices.pop();
			}
			return;
		}

		if (armorStand || (entity.isInvisible() && entity instanceof PlayerEntity)) {
			ci.cancel();
			return;
		}

		if (entity.distanceTo(mc.player) > mod.distance()) {
			ci.cancel();
		}
	}
}
