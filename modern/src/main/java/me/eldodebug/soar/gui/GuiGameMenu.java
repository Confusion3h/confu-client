package me.eldodebug.soar.gui;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.easing.EaseInOutCirc;
import me.eldodebug.soar.utils.animation.normal.easing.EaseLiner;
import me.eldodebug.soar.utils.buffer.ScreenAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.compat.GuiScreen;
import me.eldodebug.soar.compat.Keyboard;
import me.eldodebug.soar.compat.ScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

import java.awt.Color;


public class GuiGameMenu extends GuiScreen {

    private Animation introAnimation;
    private final ScreenAnimation screenAnimation = new ScreenAnimation();
    private int x, y, width, height, centre, scaledWidth, scaledHeight;

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        scaledWidth = sr.getScaledWidth();
        scaledHeight = sr.getScaledHeight();
        centre = scaledWidth / 2;
        x = centre - 90;
        y = (scaledHeight / 2) - 110;
        width = 180;
        height = 220;

        introAnimation = new EaseLiner(80, 1.0f);
        introAnimation.setDirection(Direction.FORWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NanoVGManager nvg = Glide.getInstance().getNanoVGManager();
        screenAnimation.wrap(() -> drawNanoVG(nvg), x, y, width, height, 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), false);
        if(introAnimation.isDone(Direction.BACKWARDS)) {
               this.mc.displayGuiScreen(null);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawNanoVG(NanoVGManager nvg) {
        nvg.drawRect(-5, -5, scaledWidth + 10, scaledHeight + 10,  new Color(0,0,0, 140));
        nvg.drawText(LegacyIcon.ARROW_LEFT, x, y + 5, new Color(255,255,255, 140),11, Fonts.LEGACYICON);
        nvg.drawCenteredText( I18n.translate("menu.game"), centre, y + 5,  new Color(255,255,255, 200), 13, Fonts.SEMIBOLD);

        float standardPadding = 29.5f;
        float offset = 29.5F;
        drawButton(nvg, TranslateText.MC_OPTIONS.getText(), LegacyIcon.SLIDERS, offset);
        offset += standardPadding;
        if(raw().isIntegratedServerRunning() && raw().getServer() != null && !raw().getServer().isRemote()){
            drawButton(nvg, I18n.translate("menu.shareToLan"), LegacyIcon.USERS, offset);
        } else {
            drawButton(nvg, TranslateText.EDIT_HUD.getText(), LegacyIcon.LAYOUT, offset);
        }
        offset += standardPadding;
        drawButton(nvg, I18n.translate("gui.stats"), LegacyIcon.ARCHIVE, offset);
        offset += standardPadding;
        drawButton(nvg, I18n.translate("gui.advancements"), LegacyIcon.MAP, offset);
        offset += standardPadding;
        drawButton(nvg, TranslateText.OPEN_MOD_MENU.getText(), LegacyIcon.SOAR, offset);
        offset += standardPadding;
        drawButton(nvg, !raw().isInSingleplayer() ? I18n.translate("menu.disconnect") : TranslateText.EXIT_WORLD_SINGLEPLAYER.getText(), LegacyIcon.LOGOUT, offset);
    }

    private void drawButton(NanoVGManager nvg, String s, String i, Float offset){
        nvg.drawRoundedRect(x, y + offset, width , 22, 6, new Color(230, 230, 230, 80));
        float startX = (nvg.getTextWidth(s, 9.5F, Fonts.MEDIUM) + 14) /2;
        if (LegacyIcon.SOAR.equals(i)) {
            nvg.drawLogo(centre - startX - 0.5F, y + offset + 5.5F, 11, Color.WHITE);
        } else {
            nvg.drawText(i, centre - startX, y + offset + 6.5F, Color.WHITE, 9.5F, Fonts.LEGACYICON);
        }
        nvg.drawText(s, centre - startX + 14, y + offset + 7F, Color.WHITE, 9.5F, Fonts.MEDIUM);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!(mouseButton == 0)) {return;}
        float standardPadding = 29.5f;
        float offset = standardPadding;

        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)){
            introAnimation.setDirection(Direction.BACKWARDS);
            this.mc.displayGuiScreen(new OptionsScreen(this, raw().options));
        }
        offset += standardPadding;
        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)){
            if(raw().isIntegratedServerRunning() && raw().getServer() != null && !raw().getServer().isRemote()){
                this.mc.displayGuiScreen(new OpenToLanScreen(this));
            } else {
                mc.displayGuiScreen(new GuiEditHUD(false));
            }
        }
        offset += standardPadding;
        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)) {
            this.mc.displayGuiScreen(new StatsScreen(this, raw().player.getStatHandler()));
        }
        offset += standardPadding;
        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)) {
            this.mc.displayGuiScreen(new AdvancementsScreen(raw().player.networkHandler.getAdvancementHandler()));
        }
        offset += standardPadding;
        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)) {
            mc.displayGuiScreen(Glide.getInstance().getModMenu());
        }
        offset += standardPadding;
        if (MouseUtils.isInside(mouseX, mouseY, x, y + offset, width, 22)) {
            disconnect();
        }
        if (!MouseUtils.isInside(mouseX, mouseY, x, y + standardPadding, width, offset - standardPadding + 22)) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    private MinecraftClient raw() {
        return mc.raw();
    }

    /** 1.20.1 GameMenuScreen's disconnect, which the 1.8.9 quit button mirrored. */
    private void disconnect() {
        MinecraftClient client = raw();
        boolean singleplayer = client.isInSingleplayer();
        boolean realms = client.isConnectedToRealms();
        client.world.disconnect();
        if (singleplayer) {
            client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        } else {
            client.disconnect();
        }
        TitleScreen title = new TitleScreen();
        if (singleplayer) {
            client.setScreen(title);
        } else if (realms) {
            client.setScreen(new RealmsMainScreen(title));
        } else {
            client.setScreen(new MultiplayerScreen(title));
        }
    }
}
