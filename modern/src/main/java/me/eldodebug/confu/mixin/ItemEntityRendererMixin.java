package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.ItemPhysicsModule;
import me.eldodebug.confu.module.impl.Items2DModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

	private boolean confu$flatModel;

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;hasDepth()Z"))
	private boolean confu$captureDepth(BakedModel model) {
		boolean depth = model.hasDepth();
		confu$flatModel = !depth;
		return depth;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
	private void confu$physicsTranslate(MatrixStack matrices, float x, float y, float z, ItemEntity entity, float yaw, float tickDelta, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light) {
		ItemPhysicsModule physics = ItemPhysicsModule.get();
		if (physics == null || !physics.isEnabled()) {
			matrices.translate(x, y, z);
			return;
		}
		Block block = Block.getBlockFromItem(entity.getStack().getItem());
		matrices.translate(x, block != Blocks.AIR ? 0.15F : 0.02F, z);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V"))
	private void confu$itemRotation(MatrixStack matrices, Quaternionf rotation, ItemEntity entity, float yaw, float tickDelta, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light) {
		ItemPhysicsModule physics = ItemPhysicsModule.get();
		if (physics != null && physics.isEnabled()) {
			if (Block.getBlockFromItem(entity.getStack().getItem()) == Blocks.AIR) {
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90F));
			}
			return;
		}
		Items2DModule items2d = Items2DModule.get();
		if (items2d != null && items2d.isEnabled() && confu$flatModel) {
			Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F - camera.getYaw()));
			float pitch = MinecraftClient.getInstance().options.getPerspective().isFrontView() ? camera.getPitch() : -camera.getPitch();
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
		} else {
			matrices.multiply(rotation);
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", shift = At.Shift.BEFORE))
	private void confu$physicsSpin(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		ItemPhysicsModule physics = ItemPhysicsModule.get();
		if (physics == null || !physics.isEnabled() || entity.isOnGround()) {
			return;
		}
		float angle = System.currentTimeMillis() % (360 * 20) / (4.5F - physics.speed());
		matrices.multiply(RotationAxis.of(new Vector3f(1F, 1F, 1F)).rotationDegrees(angle));
	}
}
