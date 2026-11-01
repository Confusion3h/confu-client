package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.SlowSwingModule;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
	private void confu$slowSwing(CallbackInfoReturnable<Integer> cir) {
		int delay = ConfuClient.get().modules().get(SlowSwingModule.class).delayTicks();
		if (delay > 0) {
			cir.setReturnValue(delay);
		}
	}
}
