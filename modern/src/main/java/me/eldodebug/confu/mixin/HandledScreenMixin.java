package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.util.InventoryAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/** 1.8.9 MixinGuiContainer: container open animation (normal / back-in). */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

	@Inject(method = "render", at = @At("HEAD"))
	private void confu$animationBegin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		Screen screen = (Screen) (Object) this;
		if (confu$handledByInventoryScreen(screen)) {
			return;
		}
		InventoryAnimation.push(context, screen);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void confu$animationEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		Screen screen = (Screen) (Object) this;
		if (confu$handledByInventoryScreen(screen)) {
			return;
		}
		InventoryAnimation.pop(context);
	}

	private static boolean confu$handledByInventoryScreen(Screen screen) {
		return screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen;
	}
}
