package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ClearWaterModule;
import me.eldodebug.confu.module.impl.CrosshairModule;
import me.eldodebug.confu.module.impl.ModernHotbarModule;
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

	private static ModernHotbarModule confu$modernHotbar() {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return null;
		}
		try {
			ModernHotbarModule mod = client.modules().get(ModernHotbarModule.class);
			return mod.isEnabled() ? mod : null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void confu$modernHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
		if (confu$modernHotbar() != null) {
			ci.cancel();
		}
	}

	@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
	private void confu$modernHotbarXp(DrawContext context, int x, CallbackInfo ci) {
		ModernHotbarModule mod = confu$modernHotbar();
		if (mod != null && !mod.chill()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
	private void confu$modernHotbarTooltip(DrawContext context, CallbackInfo ci) {
		if (confu$modernHotbar() != null) {
			ci.cancel();
		}
	}

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
