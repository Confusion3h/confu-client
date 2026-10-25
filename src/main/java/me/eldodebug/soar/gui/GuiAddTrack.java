package me.eldodebug.soar.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.management.music.LinkGrabber;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/** Paste box for a YouTube or SoundCloud link. The download runs off the render thread. */
public class GuiAddTrack extends GuiScreen {

	private GuiTextField url;
	private GuiScreen back;

	public GuiAddTrack() {
		this(null);
	}

	public GuiAddTrack(GuiScreen back) {
		this.back = back;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		url = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 150, this.height / 2 - 10, 300, 20);
		url.setMaxStringLength(512);
		url.setFocused(true);
		this.buttonList.add(new GuiButton(1, this.width / 2 - 150, this.height / 2 + 20, 145, 20, "Download"));
		this.buttonList.add(new GuiButton(2, this.width / 2 + 5, this.height / 2 + 20, 145, 20, "Cancel"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 1 && !LinkGrabber.active()) {
			LinkGrabber.start(url.getText());
		} else if (button.id == 2 && !LinkGrabber.active()) {
			this.mc.displayGuiScreen(back);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		String title = "Add a track";
		this.drawCenteredString(this.fontRendererObj, title, this.width / 2, this.height / 2 - 58, 0xFFFFFF);
		drawWrapped("Paste a YouTube or SoundCloud link. Confu Client saves an MP3 in your music folder. Spotify stays optional.", this.width / 2, this.height / 2 - 42, this.width - 80);
		url.drawTextBox();
		if (LinkGrabber.active()) {
			String msg = LinkGrabber.message();
			this.drawCenteredString(this.fontRendererObj, msg == null ? "Downloading" : msg, this.width / 2, this.height / 2 + 48, 0xDDDDDD);
			float p = LinkGrabber.progress();
			if (p >= 0f) {
				int bar = 220;
				int x = this.width / 2 - bar / 2;
				int y = this.height / 2 + 62;
				drawRect(x, y, x + bar, y + 6, 0xFF333333);
				drawRect(x, y, x + (int) (bar * Math.min(1f, p)), y + 6, 0xFF8FA4B8);
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawWrapped(String text, int cx, int y, int max) {
		java.util.List<String> lines = this.fontRendererObj.listFormattedStringToWidth(text, max);
		for (int i = 0; i < lines.size() && i < 3; i++) {
			this.drawCenteredString(this.fontRendererObj, lines.get(i), cx, y + i * 11, 0xBBBBBB);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			if (!LinkGrabber.active()) {
				this.mc.displayGuiScreen(back);
			}
			return;
		}
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			if (!LinkGrabber.active()) {
				LinkGrabber.start(url.getText());
			}
			return;
		}
		url.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		url.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void updateScreen() {
		url.updateCursorCounter();
	}
}
