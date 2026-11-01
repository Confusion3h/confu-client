package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.WaveyCapesModule;
import me.eldodebug.confu.module.impl.waveycapes.WaveyCapeRenderer;
import me.eldodebug.confu.module.impl.waveycapes.WaveyCapeSimulation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V",
			at = @At("HEAD"), cancellable = true)
	private void confu$waveyCapes(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch, CallbackInfo ci) {

		if (ConfuClient.get() == null || !WaveyCapesModule.get().isEnabled()) {
			return;
		}
		if (player.isInvisible() || !player.canRenderCapeTexture() || player.getCapeTexture() == null
				|| !player.isPartVisible(PlayerModelPart.CAPE)) {
			ci.cancel();
			return;
		}

		WaveyCapeRenderer.render(matrices, vertexConsumers, light, player, tickDelta,
				((WaveyCapeSimulationHolder) player).confu$capeSimulation());
		ci.cancel();
	}
}
