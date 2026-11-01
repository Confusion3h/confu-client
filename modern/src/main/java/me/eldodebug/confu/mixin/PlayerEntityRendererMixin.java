package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.skin3d.PlayerModelAccess;
import me.eldodebug.confu.module.impl.skin3d.Skin3DLayersHolder;
import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.FemaleGenderModule;
import me.eldodebug.confu.module.impl.Skin3DModule;
import me.eldodebug.confu.module.impl.skin3d.PlayerSkinLayers;
import me.eldodebug.confu.module.impl.skin3d.Skin3DBodyFeature;
import me.eldodebug.confu.module.impl.skin3d.Skin3DHeadFeature;
import me.eldodebug.confu.module.impl.skin3d.SkinModelPart;
import me.eldodebug.confu.render.FemaleGenderModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Modern player models do not receive the entity in render, so the flag is set around it. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

	@Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V", at = @At("TAIL"))
	private void confu$addSkin3DFeatures(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
		LivingEntityRendererInvoker invoker = (LivingEntityRendererInvoker) (Object) this;
		invoker.confu$callAddFeature(new Skin3DBodyFeature((PlayerEntityRenderer) (Object) this));
		invoker.confu$callAddFeature(new Skin3DHeadFeature((PlayerEntityRenderer) (Object) this));
	}

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

	/** 1.8.9 MixinRenderPlayer.setModelProperties — beyond the LOD the vanilla second-skin layers are force-shown. */
	@Inject(method = "setModelPose", at = @At("TAIL"))
	private void confu$forceSecondSkinLayers(AbstractClientPlayerEntity player, CallbackInfo ci) {
		Skin3DModule module = Skin3DModule.get();
		MinecraftClient mc = MinecraftClient.getInstance();
		if (module == null || !module.isEnabled() || mc.player == null) {
			return;
		}
		if (mc.player.squaredDistanceTo(player) < module.renderDistanceLOD() * module.renderDistanceLOD()) {
			return;
		}
		if (player.isSpectator()) {
			return;
		}
		PlayerEntityModel<AbstractClientPlayerEntity> model = ((PlayerEntityRenderer) (Object) this).getModel();
		model.hat.visible = true;
		model.jacket.visible = true;
		model.leftSleeve.visible = true;
		model.rightSleeve.visible = true;
		model.leftPants.visible = true;
		model.rightPants.visible = true;
	}

	@Inject(method = "renderRightArm", at = @At("RETURN"))
	private void confu$firstPersonRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
		confu$renderFirstPersonArm(matrices, vertexConsumers, light, player, 3);
	}

	@Inject(method = "renderLeftArm", at = @At("RETURN"))
	private void confu$firstPersonLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
		confu$renderFirstPersonArm(matrices, vertexConsumers, light, player, 2);
	}

	/** 1.8.9 MixinRenderPlayer.renderFirstPersonArm — both arms posed by the right arm part. */
	private void confu$renderFirstPersonArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, int layerId) {
		Skin3DModule module = Skin3DModule.get();
		if (module == null || !module.isEnabled()) {
			return;
		}
		PlayerEntityModel<AbstractClientPlayerEntity> model = ((PlayerEntityRenderer) (Object) this).getModel();
		boolean thinArms = ((PlayerModelAccess) model).confu$isThinArms();
		PlayerSkinLayers layers = ((Skin3DLayersHolder) player).confu$skinLayers();
		if (!layers.ensure(player, thinArms)) {
			return;
		}

		matrices.push();
		model.rightArm.rotate(matrices);
		float pixelScaling = 0.0625F * module.baseVoxelSize();
		matrices.scale(pixelScaling, pixelScaling, pixelScaling);

		SkinModelPart part = layers.body[layerId];
		part.x = (thinArms ? -0.499F : -0.998F) * 16.0F;
		part.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
		matrices.pop();
	}
}
