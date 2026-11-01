package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.util.InventoryAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

/** The creative inventory draws its background outside HandledScreen.render, so animate from here. */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

	@Inject(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;renderBackground(Lnet/minecraft/client/gui/DrawContext;)V",
			shift = At.Shift.AFTER))
	private void confu$animationBegin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryAnimation.push(context, (Screen) (Object) this);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void confu$animationEnd(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryAnimation.pop(context);
	}
}
