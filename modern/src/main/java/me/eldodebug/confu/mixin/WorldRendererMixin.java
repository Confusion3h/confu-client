package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.EntityCullingModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", at = @At("HEAD"))
	private void confu$checkQueries(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline,
			Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
			Matrix4f projectionMatrix, CallbackInfo ci) {
		EntityCullingModule.checkQueries();
	}

	@Inject(method = "renderEntity", at = @At("HEAD"))
	private void confu$beginCulling(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		EntityCullingModule.shouldPerformCulling = true;
	}

	@Inject(method = "renderEntity", at = @At("RETURN"))
	private void confu$endCulling(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		EntityCullingModule.shouldPerformCulling = false;
	}
}
