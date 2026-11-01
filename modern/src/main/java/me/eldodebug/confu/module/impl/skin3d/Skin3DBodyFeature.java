package me.eldodebug.confu.module.impl.skin3d;

import me.eldodebug.confu.mixin.PlayerModelAccess;
import me.eldodebug.confu.mixin.Skin3DLayersHolder;
import me.eldodebug.confu.module.impl.Skin3DModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;

/** 1.8.9 BodyLayerFeatureRenderer — voxel sleeves, pants and jacket over the posed limbs. */
public final class Skin3DBodyFeature
		extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	private enum Shape {
		LEGS(-0.2f), ARMS(0.4f), BODY(0.6f);

		final float yOffset;

		Shape(float yOffset) {
			this.yOffset = yOffset;
		}
	}

	private static final class Layer {
		final int index;
		final boolean mirrored;
		final PlayerModelPart toggle;
		final Shape shape;
		final boolean arm;

		Layer(int index, boolean mirrored, PlayerModelPart toggle, Shape shape, boolean arm) {
			this.index = index;
			this.mirrored = mirrored;
			this.toggle = toggle;
			this.shape = shape;
			this.arm = arm;
		}
	}

	private static final Layer[] LAYERS = {
			new Layer(0, false, PlayerModelPart.LEFT_PANTS_LEG, Shape.LEGS, false),
			new Layer(1, false, PlayerModelPart.RIGHT_PANTS_LEG, Shape.LEGS, false),
			new Layer(2, false, PlayerModelPart.LEFT_SLEEVE, Shape.ARMS, true),
			new Layer(3, true, PlayerModelPart.RIGHT_SLEEVE, Shape.ARMS, true),
			new Layer(4, false, PlayerModelPart.JACKET, Shape.BODY, false)};

	public Skin3DBodyFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
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
		if (mc.player.squaredDistanceTo(player) > module.renderDistanceLOD()) {
			return;
		}

		PlayerEntityModel<AbstractClientPlayerEntity> model = getContextModel();
		PlayerSkinLayers layers = ((Skin3DLayersHolder) player).confu$skinLayers();
		if (!layers.ensure(player, ((PlayerModelAccess) model).confu$isThinArms())) {
			return;
		}

		VertexConsumer vertices = providers.getBuffer(RenderLayer.getEntityCutoutNoCull(player.getSkinTexture()));
		float pixelScaling = module.baseVoxelSize();
		float heightScaling = 1.035f;
		boolean thinArms = ((PlayerModelAccess) model).confu$isThinArms();

		for (int i = 0; i < LAYERS.length; i++) {
			Layer def = LAYERS[i];
			ModelPart vanilla = vanillaPart(model, def);
			if (!player.isPartVisible(def.toggle) || !vanilla.visible) {
				continue;
			}

			matrices.push();
			vanilla.rotate(matrices);

			SkinModelPart part = layers.body[def.index];
			if (def.arm) {
				part.x = (thinArms ? 0.499f : 0.998f) * 16f;
			}
			float widthScaling = def.shape == Shape.BODY ? module.bodyVoxelWidthSize() : pixelScaling;
			if (def.mirrored) {
				part.x *= -1;
			}

			matrices.scale(0.0625f * widthScaling, 0.0625f * heightScaling, 0.0625f * pixelScaling);
			part.y = def.shape.yOffset;
			part.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
			matrices.pop();
		}
	}

	private static ModelPart vanillaPart(PlayerEntityModel<AbstractClientPlayerEntity> model, Layer def) {
		return switch (def.index) {
			case 0 -> model.leftLeg;
			case 1 -> model.rightLeg;
			case 2 -> model.leftArm;
			case 3 -> model.rightArm;
			default -> model.body;
		};
	}
}
