package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.module.impl.skin3d.Skin3DUtils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;

/** Captures the finished (legacy-remapped, striped) skin image, the 1.20.1 stand-in for the 1.8.9 NativeImage GL download. */
@Mixin(PlayerSkinTexture.class)
public abstract class PlayerSkinTextureMixin implements Skin3DUtils.PlayerSkinImage {

	@Unique
	private NativeImage confu$capturedSkin;

	@Inject(method = "uploadTexture", at = @At("HEAD"))
	private void confu$captureSkin(NativeImage image, CallbackInfo ci) {
		confu$capturedSkin = image;
	}

	@Override
	public NativeImage confu$skinImage() {
		return confu$capturedSkin;
	}
}
