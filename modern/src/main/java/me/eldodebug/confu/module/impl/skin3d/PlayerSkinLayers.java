package me.eldodebug.confu.module.impl.skin3d;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;

/** 1.8.9 IMixinEntityPlayer skin layers — per player voxel parts built from the uploaded skin. */
public final class PlayerSkinLayers {

	public SkinModelPart[] body;
	public SkinModelPart head;

	public boolean ensure(AbstractClientPlayerEntity player, boolean thinArms) {
		if (body != null && head != null) {
			return true;
		}

		if (!Skin3DUtils.hasCustomSkin(player)) {
			return false;
		}

		NativeImage skin = Skin3DUtils.skinImage(player);
		if (skin == null) {
			return false;
		}

		SkinModelPart[] layers = new SkinModelPart[5];
		layers[0] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 48, true, 0f);
		layers[1] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 32, true, 0f);

		if (thinArms) {
			layers[2] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 48, 48, true, -2.5f);
			layers[3] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 40, 32, true, -2.5f);
		} else {
			layers[2] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 48, 48, true, -2.5f);
			layers[3] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 40, 32, true, -2.5f);
		}

		layers[4] = SolidPixelWrapper.wrapBox(skin, 8, 12, 4, 16, 32, true, -0.8f);

		body = layers;
		head = SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f);
		return true;
	}
}
