package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ClearWaterModule;
import me.eldodebug.confu.module.impl.CrosshairModule;
import me.eldodebug.confu.module.impl.OverlayEditorModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
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

	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private void confu$overlays(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
		OverlayEditorModule overlay = ConfuClient.get().modules().get(OverlayEditorModule.class);
		String path = texture.getPath();
		if (overlay.hidePumpkin() && path.contains("pumpkin")) {
			ci.cancel();
			return;
		}
		if (overlay.hideFire() && path.contains("fire")) {
			ci.cancel();
			return;
		}
		if (ConfuClient.get().modules().get(ClearWaterModule.class).isEnabled() && path.contains("underwater")) {
			ci.cancel();
		}
	}
}
