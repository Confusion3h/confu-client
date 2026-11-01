package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.InventoryModule;
import me.eldodebug.confu.util.InventoryParticles;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

/** 1.8.9 MixinGuiContainer: inventory background toggle and particle field. */
@Mixin(Screen.class)
public abstract class ScreenMixin {

	@Redirect(method = "renderBackground", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V"))
	private void confu$inventoryBackground(DrawContext context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
		if ((Object) this instanceof HandledScreen) {
			InventoryModule mod = confu$inventory();
			if (mod != null && mod.isEnabled() && !mod.background()) {
				return;
			}
		}
		context.fillGradient(startX, startY, endX, endY, colorStart, colorEnd);
	}

	@Inject(method = "renderBackground", at = @At("RETURN"))
	private void confu$inventoryParticles(DrawContext context, CallbackInfo ci) {
		if ((Object) this instanceof HandledScreen) {
			InventoryParticles.render(context, (Screen) (Object) this);
		}
	}

	private static InventoryModule confu$inventory() {
		try {
			return ConfuClient.get().modules().get(InventoryModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
