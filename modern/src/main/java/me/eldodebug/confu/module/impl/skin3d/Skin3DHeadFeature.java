package me.eldodebug.confu.module.impl.skin3d;

import me.eldodebug.confu.module.impl.Skin3DModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;

/** 1.8.9 HeadLayerFeatureRenderer — voxel hat, skipped beyond LOD² and while a skull hides the hat. */
public final class Skin3DHeadFeature
		extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public Skin3DHeadFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider providers, int light, AbstractClientPlayerEntity player,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

		Skin3DModule module = Skin3DModule.get();
		MinecraftClient mc = MinecraftClient.getInstance();
		if (module == null || !module.isEnabled() || mc.player == null) {
			return;
		}
		if (!player.hasSkinTexture() || player.isInvisible()) {
			return;
		}
		if (mc.player.squaredDistanceTo(player) > module.renderDistanceLOD() * module.renderDistanceLOD()) {
			return;
		}

		PlayerEntityModel<AbstractClientPlayerEntity> model = getContextModel();
		if (!model.head.visible || !model.hat.visible) {
			return;
		}

		PlayerSkinLayers layers = ((Skin3DLayersHolder) player).confu$skinLayers();
		if (!layers.ensure(player, ((PlayerModelAccess) model).confu$isThinArms())) {
			return;
		}

		float voxelSize = module.headVoxelSize();
		matrices.push();
		model.head.rotate(matrices);
		matrices.scale(0.0625f * voxelSize, 0.0625f * voxelSize, 0.0625f * voxelSize);
		layers.head.render(matrices,
				providers.getBuffer(RenderLayer.getEntityCutoutNoCull(player.getSkinTexture())),
				light, OverlayTexture.DEFAULT_UV);
		matrices.pop();
	}
}
