package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.AnimationsModule;
import me.eldodebug.confu.module.impl.FarCameraModule;
import me.eldodebug.confu.module.impl.FreelookModule;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	private Entity focusedEntity;

	@Shadow
	private float cameraY;

	@Shadow
	private float lastCameraY;

	@ModifyArgs(
			method = "update",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V")
	)
	private void confu$freelookRotation(Args args) {
		float yaw = args.get(0);
		float pitch = args.get(1);
		float[] rotation = ConfuClient.get().modules().get(FreelookModule.class).cameraRotation(yaw, pitch);
		args.set(0, rotation[0]);
		args.set(1, rotation[1]);
	}

	@ModifyVariable(method = "clipToSpace", at = @At("HEAD"), argsOnly = true)
	private double confu$farCamera(double desiredCameraDistance) {
		return ConfuClient.get().modules().get(FarCameraModule.class).distance(desiredCameraDistance);
	}

	/** 1.8.9 Animations: configurable sneaking eye-height smoothing. */
	@Inject(method = "updateEyeHeight", at = @At("TAIL"))
	private void confu$smoothSneak(CallbackInfo ci) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null || !(focusedEntity instanceof LivingEntity living)) {
			return;
		}
		try {
			AnimationsModule mod = client.modules().get(AnimationsModule.class);
			if (!mod.smoothSneak() || living.isSleeping()) {
				return;
			}
			float target = living.getEyeHeight(living.getPose());
			float previous = this.cameraY;
			this.cameraY = previous + (target - previous) * Math.min(0.6F, mod.smoothSpeed() / 20F);
			this.lastCameraY = previous;
		} catch (RuntimeException ignored) {
		}
	}
}
