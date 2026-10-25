package me.eldodebug.soar.injection.mixin.mixins.gui;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

@Mixin(GuiOptions.class)
public class MixinGuiOptions extends GuiScreen {

    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }

    /**
     * Vanilla rows all use {@code height/6 + constant}, so the gap does not
     * change with GUI scale. Resource packs and Snooper sit at +138 (20px,
     * bottom +158). This row is +162. Done (id 200) is moved from +168 to +186.
     */
    @Inject(method = "initGui", at = @At("TAIL"))
    private void confusionButtons(CallbackInfo ci) {
        int y = this.height / 6 + 162;
        this.buttonList.add(new GuiButton(401, this.width / 2 - 155, y, 150, 20, TranslateText.OPEN_MOD_MENU.getText()));
        String quit = this.mc.isIntegratedServerRunning() ? TranslateText.EXIT_WORLD_SINGLEPLAYER.getText() : I18n.format("menu.disconnect");
        this.buttonList.add(new GuiButton(402, this.width / 2 + 5, y, 150, 20, quit));
        for (Object o : this.buttonList) {
            if (o instanceof GuiButton && ((GuiButton) o).id == 200) {
                ((GuiButton) o).yPosition = this.height / 6 + 186;
            }
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void confusionAction(GuiButton button, CallbackInfo ci) throws IOException {
        if (button.id == 401) {
            this.mc.displayGuiScreen(Glide.getInstance().getModMenu());
            ci.cancel();
        } else if (button.id == 402) {
            boolean single = this.mc.isIntegratedServerRunning();
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);
            if (single) {
                this.mc.displayGuiScreen(new GuiMainMenu());
            } else {
                this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
            }
            ci.cancel();
        }
    }
}
