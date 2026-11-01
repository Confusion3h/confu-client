package me.eldodebug.soar.ui.comp.impl.field;

import me.eldodebug.soar.management.mods.settings.impl.TextSetting;

/** 1.8.9 CompModTextBox: text box bound to a module's text setting. */
public class CompModTextBox extends CompTextBox {

	private final TextSetting setting;

	public CompModTextBox(TextSetting setting) {
		super();
		this.setting = setting;
		this.setText(setting.getText());
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		super.draw(mouseX, mouseY, partialTicks);

		if (!isFocused() && !getText().equals(setting.getText())) {
			setting.setText(getText());
			setting.commit();
		}
	}
}
