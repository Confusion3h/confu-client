package me.eldodebug.confu.module.impl.skin3d;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.DefaultSkinHelper;

/** 1.8.9 SkinUtils — voxel layer construction from the uploaded skin image. */
public final class Skin3DUtils {

	private Skin3DUtils() {
	}

	public static boolean hasCustomSkin(AbstractClientPlayerEntity player) {
		return !DefaultSkinHelper.getTexture(player.getUuid()).equals(player.getSkinTexture());
	}

	public static NativeImage skinImage(AbstractClientPlayerEntity player) {
		AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(player.getSkinTexture());
		return texture instanceof PlayerSkinImage image ? image.confu$skinImage() : null;
	}

	/** Implemented on PlayerSkinTexture by a mixin; the image handed to uploadTexture is the remapped GL texture. */
	public interface PlayerSkinImage {
		NativeImage confu$skinImage();
	}
}
