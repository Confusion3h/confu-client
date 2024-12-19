package me.eldodebug.soar.gui.modmenu.category.impl;

import java.awt.Color;
import java.awt.Desktop;
import java.net.URL;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.remote.changelog.Changelog;
import me.eldodebug.soar.management.remote.changelog.ChangelogManager;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class HomeCategory extends Category {

	public HomeCategory(GuiModMenu parent) {
		super(parent, TranslateText.HOME, Icon.HOME, false, false);
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

		nvg.drawRoundedRect(this.getX() + 230, this.getY() + 15, 174, 151, 8, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawText(TranslateText.CHANGELOG.getText(), this.getX() + 230 + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 11F, Fonts.DEMIBOLD);

		for(Changelog c : changelogManager.getChangelogs()) {
			float tbSize = nvg.getTextBoxHeight(c.getText(), 8, Fonts.MEDIUM, 174 - 33);
			nvg.drawRoundedRect(this.getX() + 230 + 8, this.getY() + 45 + offsetY + ((tbSize/2)-4), 13, 13, 7F, c.getType().getColor());
			nvg.drawCenteredText(c.getType().getText(), this.getX() + 230 + 8 + (13 / 2), this.getY() + 47F + offsetY + ((tbSize/2)-4), Color.WHITE, 9, Fonts.ICON);
			nvg.drawTextBox(c.getText(), this.getX() + 230 + 25, this.getY() + 48F + offsetY, 174 - 33, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
			offsetY+= (int) (tbSize + 8);
		}

		// Discord
		int discordStartX = this.getX() + 230;
		int discordStartY = this.getY() + 179;
		nvg.drawGradientRoundedRect(discordStartX, discordStartY, 174, 86, 8, currentColor.getColor1(), currentColor.getColor2());
		nvg.drawText(TranslateText.JOIN_OUR_DISCORD_SERVER.getText(), discordStartX + 8, discordStartY + 8, Color.WHITE, 11F, Fonts.DEMIBOLD);
		nvg.drawTextBox(TranslateText.DISCORD_DESCRIPTION.getText(), discordStartX + 8, discordStartY + 24, 174 - 16, Color.WHITE, 9, Fonts.REGULAR);

		nvg.drawRoundedRect(discordStartX + 174 - 60, discordStartY + 58, 52, 18, 10, Color.WHITE);
		nvg.drawCenteredText(TranslateText.JOIN.getText() + " >", discordStartX + 174 - 60 + (52 / 2), discordStartY + 63, currentColor.getInterpolateColor(), 7, Fonts.REGULAR);


	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		int discordStartX = this.getX() + 230;
		int discordStartY = this.getY() + 179;
			if(MouseUtils.isInside(mouseX, mouseY, discordStartX + 174 - 60, discordStartY + 58, 52, 18)) {
				try {
					Desktop.getDesktop().browse(new URL("https://glideclient.github.io/discord").toURI());
				} catch (Exception e) {}}
	}
}
