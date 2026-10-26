package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.FreelookModule;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

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
}
