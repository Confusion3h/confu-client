package me.eldodebug.soar.injection.mixin.mixins.gui;

import java.io.IOException;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.EscapeLatch;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.utils.Sound;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

	@Shadow
    protected Minecraft mc;
    
	@Shadow
    public abstract void keyTyped(char typedChar, int keyCode);
    
	@Inject(method = "drawScreen", at = @At("TAIL"))
    public void postDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if(InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled() && !InternalSettingsMod.isFpsMode()) {
			Glide.getInstance().getClickEffects().drawClickEffects();
		}
	}
	
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		if(InternalSettingsMod.getInstance().getClickEffectsSetting().isToggled() && !InternalSettingsMod.isFpsMode()) {
			Glide.getInstance().getClickEffects().addClickEffect(mouseX, mouseY);
		}
		Sound.play("soar/audio/click.wav", true);
	}
	
	@Overwrite
    public void handleKeyboardInput() throws IOException {
        char c = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean down = Keyboard.getEventKeyState();

        // The ESC that opened this screen can still be queued. keyTyped(ESC)
        // calls displayGuiScreen(null); ignore it until the tick ends.
        if (!(EscapeLatch.swallow && down && key == Keyboard.KEY_ESCAPE)) {
            if ((key == 0 && c >= ' ') || down) {
                this.keyTyped(c, key);
            }
        }

        mc.dispatchKeypresses();
    }
}
