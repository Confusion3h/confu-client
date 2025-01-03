package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.modmenu.category.impl.ModuleCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import me.eldodebug.soar.ui.comp.impl.CompKeybind;

public class GeneralScene extends SettingScene {

	private CompKeybind modMenuKeybind;

	public GeneralScene(SettingCategory parent) {
		super(parent, TranslateText.GENERAL, TranslateText.GENERAL_DESCRIPTION, LegacyIcon.LIST);
	}

	@Override
	public void initGui() {
		modMenuKeybind = new CompKeybind(75, GlobalSettingsMod.getInstance().getModMenuKeybindSetting());
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		ColorPalette palette = colorManager.getPalette();
		int offsetY = 0;
		//  mod menu keybind
		nvg.drawRoundedRect(this.getX(), this.getY() + offsetY, this.getWidth(), 41, 6, palette.getBackgroundColor(ColorType.DARK));
		nvg.drawText(TranslateText.OPEN_MOD_MENU.getText(), this.getX() + 8, this.getY() + 20.5F + offsetY - (nvg.getTextHeight(TranslateText.OPEN_MOD_MENU.getText(), 13, Fonts.MEDIUM)/2), palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
		modMenuKeybind.setX(this.getX() + this.getWidth() - 87);
		modMenuKeybind.setY(this.getY() + 12.5F + offsetY);
		modMenuKeybind.draw(mouseX, mouseY, partialTicks);
		offsetY += 51;

	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		Glide instance = Glide.getInstance();
		modMenuKeybind.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (modMenuKeybind.isBinding()) {
			modMenuKeybind.keyTyped(typedChar, keyCode);
		}
	}
}
