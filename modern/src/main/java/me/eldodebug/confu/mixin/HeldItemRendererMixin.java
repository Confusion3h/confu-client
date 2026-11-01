package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.CustomHeldItemsModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	private int confu$pushed;

	@Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"))
	private void confu$customHeldItems(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
		CustomHeldItemsModule mod = CustomHeldItemsModule.get();
		if (mod == null || !mod.isEnabled()) {
			return;
		}
		matrices.push();
		confu$pushed++;
		matrices.translate(mod.x(), mod.y(), mod.z());
		matrices.scale(mod.xScale(), mod.yScale(), mod.zScale());
	}

	@Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("RETURN"))
	private void confu$customHeldItemsEnd(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
		if (confu$pushed > 0) {
			confu$pushed--;
			matrices.pop();
		}
	}
}
