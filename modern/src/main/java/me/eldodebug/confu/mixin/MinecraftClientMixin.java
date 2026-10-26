package me.eldodebug.confu.mixin;

import me.eldodebug.soar.gui.GuiGameMenu;
import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
}
