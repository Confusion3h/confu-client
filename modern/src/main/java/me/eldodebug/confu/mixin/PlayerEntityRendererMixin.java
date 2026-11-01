package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.FemaleGenderModule;
import me.eldodebug.confu.render.FemaleGenderModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Modern player models do not receive the entity in render, so the flag is set around it. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

	@Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
	private void confu$beginFemaleGender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		FemaleGenderModule mod = ConfuClient.get().modules().get(FemaleGenderModule.class);
		FemaleGenderModel model = (FemaleGenderModel) ((PlayerEntityRenderer) (Object) this).getModel();
		model.confu$setFemaleGender(mod.isEnabled() && player == MinecraftClient.getInstance().player, player.isSneaking());
	}

	@Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
	private void confu$endFemaleGender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		FemaleGenderModel model = (FemaleGenderModel) ((PlayerEntityRenderer) (Object) this).getModel();
		model.confu$setFemaleGender(false, false);
	}
}
