package me.eldodebug.confu.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameMenuScreen.class)
public interface GameMenuScreenAccessor {

	@Accessor("showMenu")
	boolean confu$showMenu();
}
