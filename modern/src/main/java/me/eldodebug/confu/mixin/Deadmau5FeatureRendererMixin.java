package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.EarsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Swaps the vanilla deadmau5 layer for the 1.8.9 EarsMod layer, which hides vanilla ears entirely. */
@Mixin(Deadmau5FeatureRenderer.class)
public abstract class Deadmau5FeatureRendererMixin {

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
	private void confu$ears(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		ci.cancel();

		EarsModule ears = ConfuClient.get().modules().get(EarsModule.class);
		if (!ears.isEnabled() || player != MinecraftClient.getInstance().player || player.isInvisible()) {
			return;
		}

		PlayerEntityModel<AbstractClientPlayerEntity> model = ((Deadmau5FeatureRenderer) (Object) this).getContextModel();
		int overlay = LivingEntityRenderer.getOverlay(player, 0.0F);
		float yaw = MathHelper.lerp(tickDelta, player.prevYaw, player.getYaw()) - MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw);
		float pitch = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());

		for (int i = 0; i < 2; i++) {
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
			matrices.translate(0.375F * (i * 2 - 1), 0.0F, 0.0F);
			matrices.translate(0.0F, -0.375F, 0.0F);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
			matrices.scale(1.3333334F, 1.3333334F, 1.3333334F);
			if (player.isSneaking()) {
				matrices.translate(0.0F, 0.2F, 0.0F);
			}
			VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(i == 0 ? EarsModule.LEFT_TEXTURE : EarsModule.RIGHT_TEXTURE));
			model.renderEars(matrices, buffer, light, overlay);
			matrices.pop();
		}
	}
}
