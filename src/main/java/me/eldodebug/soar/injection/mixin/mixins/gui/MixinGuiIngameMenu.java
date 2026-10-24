package me.eldodebug.soar.injection.mixin.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiIngameMenu.class)
public abstract class MixinGuiIngameMenu {

    /** ESC opens vanilla Options. Disconnect and the client menu are buttons on that screen; Right Shift still opens the client menu. */
    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    public void preInitGui(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiOptions(null, mc.gameSettings));
        ci.cancel();
    }

}
