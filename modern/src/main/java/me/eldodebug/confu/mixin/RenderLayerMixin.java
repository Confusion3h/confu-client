package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.confu.module.impl.ShinyPotsModule;
import net.minecraft.client.render.RenderLayer;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {

	@Inject(method = "getGlint", at = @At("HEAD"), cancellable = true)
	private static void confu$shinyGlint(CallbackInfoReturnable<RenderLayer> cir) {
		if (ShinyPotsModule.active()) {
			cir.setReturnValue(RenderLayer.getEntityGlint());
		}
	}

	@Inject(method = "getDirectGlint", at = @At("HEAD"), cancellable = true)
	private static void confu$shinyDirectGlint(CallbackInfoReturnable<RenderLayer> cir) {
		if (ShinyPotsModule.active()) {
			cir.setReturnValue(RenderLayer.getDirectEntityGlint());
		}
	}
}
