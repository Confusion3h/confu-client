package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.net.URL;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.changelog.Changelog;
import me.eldodebug.soar.management.changelog.ChangelogManager;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class HomeCategory extends Category {

	public HomeCategory(GuiModMenu parent) {
		super(parent, TranslateText.HOME, Icon.HOME, false);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		AccentColor currentColor = colorManager.getCurrentColor();
		ChangelogManager changelogManager = instance.getChangelogManager();

		// Changelog
		
		int offsetY = 0;
		
		nvg.drawRoundedRect(this.getX() + 230, this.getY() + 15, 174, 136, 8, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawRect(this.getX() + 230, this.getY() + 15 + 24, 174, 1, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(TranslateText.CHANGELOG.getText(), this.getX() + 230 + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 13.5F, Fonts.MEDIUM);
		
		for(Changelog c : changelogManager.getChangelogs()) {
			
			nvg.drawRoundedRect(this.getX() + 230 + 8, this.getY() + 45 + offsetY, 48, 13, 2.5F, c.getType().getColor());
			nvg.drawCenteredText(c.getType().getText(), this.getX() + 230 + 8 + (48 / 2), this.getY() + 48.5F + offsetY, Color.WHITE, 8, Fonts.MEDIUM);
			nvg.drawText(c.getText(), this.getX() + 230 + 61, this.getY() + 48F + offsetY, palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
			
			offsetY+=16;
		}
		
		// Discord
		
		nvg.drawGradientRoundedRect(this.getX() + 15, this.getY() + 164, 389, 70, 8, currentColor.getColor1(), currentColor.getColor2());
		nvg.drawText(TranslateText.JOIN_OUR_DISCORD_SERVER.getText(), this.getX() + 15 + 50, this.getY() + 164 + 11, Color.WHITE, 13.5F, Fonts.MEDIUM);
		nvg.drawText(TranslateText.DISCORD_DESCRIPTION.getText(), this.getX() + 15 + 50, this.getY() + 164 + 27, Color.WHITE, 9, Fonts.REGULAR);
		nvg.drawRoundedRect(this.getX() + 15 + 10, this.getY() + 164 + 11, 34, 34, 34 / 2, Color.WHITE);
		nvg.drawText(Icon.DISCORD, this.getX() + 15 + 18.5F, this.getY() + 164 + 20.5F, currentColor.getInterpolateColor(), 17, Fonts.ICON);
		
		nvg.drawRoundedRect(this.getX() + 15 + 50, this.getY() + 164 + 43, 52, 18, 8, Color.WHITE);
		nvg.drawCenteredText(TranslateText.JOIN.getText() + " >", this.getX() + 15 + 50 + (52 / 2), this.getY() + 164 + 48, currentColor.getInterpolateColor(), 9, Fonts.REGULAR);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + 50, this.getY() + 164 + 43, 52, 18)) {
				try {
					Desktop.getDesktop().browse(new URL("https://discord.gg/soar-client-967307105516281917").toURI());
				} catch (Exception e) {}}
	}
}
