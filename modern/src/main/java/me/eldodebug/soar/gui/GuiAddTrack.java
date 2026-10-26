package me.eldodebug.soar.gui;

import java.util.List;

import me.eldodebug.soar.management.music.LinkGrabber;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Paste box for a YouTube or SoundCloud link (1.8.9 GuiAddTrack). The download runs off the render thread. */
public class GuiAddTrack extends Screen {

	private TextFieldWidget url;
	private final Screen back;
	private boolean awaiting;
	private int addedTicks;

	public GuiAddTrack(Screen back) {
		super(Text.literal("Add a track"));
		this.back = back;
	}

	@Override
	protected void init() {
		String old = url == null ? "" : url.getText();
		url = new TextFieldWidget(textRenderer, width / 2 - 150, height / 2 - 10, 300, 20, Text.literal(""));
		url.setMaxLength(512);
		url.setText(old);
		addDrawableChild(url);
		setInitialFocus(url);
		addedTicks = 0;
		awaiting = LinkGrabber.active();
		if (!awaiting) {
			LinkGrabber.clearIdle();
		}
		addDrawableChild(ButtonWidget.builder(Text.literal("Download"), b -> {
			if (addedTicks == 0 && !LinkGrabber.active()) {
				awaiting = LinkGrabber.start(url.getText());
			}
		}).dimensions(width / 2 - 150, height / 2 + 20, 145, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
			if (addedTicks > 0 || !LinkGrabber.active()) {
				client.setScreen(back);
			}
		}).dimensions(width / 2 + 5, height / 2 + 20, 145, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, "Add a track", width / 2, height / 2 - 58, 0xFFFFFF);
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal("Paste a YouTube or SoundCloud link. Confu Client saves an MP3 in your music folder."), width - 80);
		for (int i = 0; i < lines.size() && i < 3; i++) {
			context.drawCenteredTextWithShadow(textRenderer, lines.get(i), width / 2, height / 2 - 42 + i * 11, 0xBBBBBB);
		}
		String msg = addedTicks > 0 ? "Added" : LinkGrabber.message();
		if (msg != null && msg.length() > 0) {
			context.drawCenteredTextWithShadow(textRenderer, msg, width / 2, height / 2 + 48, addedTicks > 0 ? 0xFFFFFF : 0xDDDDDD);
		}
		float p = addedTicks > 0 ? 1f : (LinkGrabber.active() ? LinkGrabber.progress() : -1f);
		if (p >= 0f) {
			int bar = 220;
			int x = width / 2 - bar / 2;
			int y = height / 2 + 62;
			context.fill(x, y, x + bar, y + 6, 0xFF333333);
			context.fill(x, y, x + (int) (bar * Math.min(1f, p)), y + 6, 0xFF8FA4B8);
		}
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (!LinkGrabber.active()) {
				client.setScreen(back);
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			if (addedTicks == 0 && !LinkGrabber.active()) {
				awaiting = LinkGrabber.start(url.getText());
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void tick() {
		url.tick();
		if (awaiting && LinkGrabber.takeSuccess()) {
			awaiting = false;
			addedTicks = 16;
		}
		if (addedTicks > 0) {
			addedTicks--;
			if (addedTicks == 0) {
				client.setScreen(back);
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
