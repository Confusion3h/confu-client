package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.CrosshairModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void confu$hideVanillaCrosshair(DrawContext context, CallbackInfo ci) {
		if (ConfuClient.get().modules().get(CrosshairModule.class).hidesVanillaCrosshair()) {
			ci.cancel();
		}
	}
}
