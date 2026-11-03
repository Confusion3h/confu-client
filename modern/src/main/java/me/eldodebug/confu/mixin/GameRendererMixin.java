package me.eldodebug.confu.mixin;

import org.joml.Matrix4f;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.BowZoomModule;
import me.eldodebug.confu.module.impl.FovModifierModule;
import me.eldodebug.confu.module.impl.MinimalDamageShakeModule;
import me.eldodebug.confu.module.impl.MinimalViewBobbingModule;
import me.eldodebug.confu.module.impl.RearviewModule;
import me.eldodebug.confu.module.impl.ZoomModule;
import me.eldodebug.confu.module.impl.rearview.RearviewCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	/** 1.8.9 EventRenderTick fired in runGameLoop before the frame renders. */
	@Inject(method = "render", at = @At("HEAD"))
	private void confu$rearviewTick(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		RearviewModule module = RearviewModule.get();
		if (module != null && module.isEnabled()) {
			module.onRenderTick(tickDelta);
		}
	}

	/** 1.8.9 swapped mc.displayWidth/Height to 800x600; the window is immutable here. */
	@Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
	private void confu$rearviewProjection(double fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
		if (RearviewCamera.isRecording()) {
			cir.setReturnValue(new Matrix4f().perspective((float) (fovDegrees * 0.01745329238474369),
					RearviewCamera.MIRROR_WIDTH / (float) RearviewCamera.MIRROR_HEIGHT, 0.05F,
					((GameRenderer) (Object) this).getFarPlaneDistance()));
		}
	}

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void confu$applyZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		ZoomModule zoom = ConfuClient.get().modules().get(ZoomModule.class);
		float multiplier = zoom.fovMultiplier();
		if (multiplier != 1F) {
			cir.setReturnValue(cir.getReturnValue() * multiplier);
		}
		FovModifierModule fov = ConfuClient.get().modules().get(FovModifierModule.class);
		float scale = fov.fovScale();
		if (scale != 1F) {
			cir.setReturnValue(cir.getReturnValue() * scale);
		}
		float bow = ConfuClient.get().modules().get(BowZoomModule.class).fovMultiplier();
		if (bow != 1F) {
			cir.setReturnValue(cir.getReturnValue() * bow);
		}
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void confu$minimalBob(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (ConfuClient.get().modules().get(MinimalViewBobbingModule.class).isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
	private void confu$minimalHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		MinimalDamageShakeModule mod = ConfuClient.get().modules().get(MinimalDamageShakeModule.class);
		if ((mod.isEnabled() && mod.intensityFactor() <= 0F) || RearviewCamera.isRecording()) {
			ci.cancel();
		}
	}

	/** 1.8.9 MixinEntityRenderer#adjustHurtCameraEffect: scale every rotation in the hurt/death tilt by intensity/100. */
	@ModifyArg(method = "tiltViewWhenHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"), index = 0)
	private float confu$scaleHurt(float angle) {
		return angle * ConfuClient.get().modules().get(MinimalDamageShakeModule.class).intensityFactor();
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void confu$notifications(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		me.eldodebug.soar.Glide.getInstance().getNotificationManager().render();
	}
}
