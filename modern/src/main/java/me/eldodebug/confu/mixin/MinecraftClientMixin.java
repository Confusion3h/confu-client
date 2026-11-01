package me.eldodebug.confu.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ColorSaturationModule;
import me.eldodebug.confu.module.impl.MotionBlurModule;
import me.eldodebug.soar.gui.GuiGameMenu;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Swaps the vanilla title and pause screens for the 1.8.9 Confu main menu and game menu. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen confu$replaceScreen(Screen screen) {
		if (screen instanceof TitleScreen) {
			return new GuiGlideMainMenu();
		}
		if (screen instanceof GameMenuScreen && ((GameMenuScreenAccessor) screen).confu$showMenu()) {
			return new GuiGameMenu();
		}
		return screen;
	}

	/** 1.8.9 EventShader runs after the HUD and before the final blit. */
	@Inject(method = "render(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;endWrite()V", shift = At.Shift.BEFORE))
	private void confu$renderPostEffects(boolean tick, CallbackInfo ci) {
		ColorSaturationModule colorSaturation = ConfuClient.get().modules().get(ColorSaturationModule.class);
		MotionBlurModule motionBlur = ConfuClient.get().modules().get(MotionBlurModule.class);

		boolean saturation = colorSaturation.isEnabled();
		boolean blur = motionBlur.isEnabled() && motionBlur.isShaderMode();
		if (!saturation && !blur) {
			return;
		}

		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.resetTextureMatrix();
		if (saturation) {
			colorSaturation.renderShader();
		}
		if (blur) {
			motionBlur.renderShader();
		}
	}

	/** 1.8.9 EventUpdateDisplay runs right before the buffers are swapped. */
	@Inject(method = "render(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers()V", shift = At.Shift.BEFORE))
	private void confu$updateDisplay(boolean tick, CallbackInfo ci) {
		MotionBlurModule motionBlur = ConfuClient.get().modules().get(MotionBlurModule.class);
		if (motionBlur.isEnabled()) {
			motionBlur.onUpdateDisplay();
		}
	}
}
