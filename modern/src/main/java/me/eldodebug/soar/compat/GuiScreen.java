package me.eldodebug.soar.compat;

import java.io.IOException;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * 1.8.9 GuiScreen lifecycle on a 1.20.1 Screen: initGui, drawScreen, mouseClicked/Released and
 * keyTyped(char, keyCode) receive the same scaled coordinates the 1.8.9 client used.
 */
public abstract class GuiScreen extends Screen {

	protected final Minecraft mc = Minecraft.getMinecraft();
	private boolean drawingSuper;
	private DrawContext drawContext;

	/** The 1.20.1 draw context of the frame being drawn, for vanilla-rendered parts (HUD, items). */
	protected DrawContext getDrawContext() {
		return drawContext;
	}

	protected GuiScreen() {
		super(Text.empty());
	}

	public void initGui() {
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
	}

	/** 1.8.9 default: Escape closes the screen. */
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}
	}

	public void handleInput() throws IOException {
	}

	public void onGuiClosed() {
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	protected final void init() {
		initGui();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (drawingSuper) {
			return;
		}
		context.draw();
		drawContext = context;
		drawingSuper = true;
		try {
			drawScreen(mouseX, mouseY, delta);
		} finally {
			drawingSuper = false;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			mouseClicked((int) mouseX, (int) mouseY, button);
		} catch (IOException ignored) {
		}
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseReleased((int) mouseX, (int) mouseY, button);
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		Mouse.addWheel(amount);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		keyTyped('\0', keyCode);
		return true;
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		keyTyped(chr, 0);
		return true;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean shouldPause() {
		return doesGuiPauseGame();
	}

	@Override
	public void removed() {
		onGuiClosed();
	}
}
