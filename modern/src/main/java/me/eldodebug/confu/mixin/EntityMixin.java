package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.FreelookModule;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	private void confu$freelookMouse(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		FreelookModule freelook = ConfuClient.get().modules().get(FreelookModule.class);
		if (freelook.captureLook((Entity) (Object) this, cursorDeltaX, cursorDeltaY)) {
			ci.cancel();
		}
	}
}
