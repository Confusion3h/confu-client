package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.CpsModule;
import me.eldodebug.confu.module.impl.ZoomModule;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	private void confu$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (ConfuClient.get().modules().get(ZoomModule.class).onScroll(vertical)) {
			ci.cancel();
		}
	}

	@Inject(method = "onMouseButton", at = @At("HEAD"))
	private void confu$countClick(long window, int button, int action, int mods, CallbackInfo ci) {
		// 1.8.9 counted in-game clicks only (EventClickMouse), not clicks on menus
		if (action == 1 && net.minecraft.client.MinecraftClient.getInstance().currentScreen == null) {
			CpsModule.click(button);
		}
	}
}
