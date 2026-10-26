package me.eldodebug.confu.gui;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.impl.CrosshairModule;
import me.eldodebug.confu.module.impl.FreelookModule;
import me.eldodebug.confu.module.impl.KeystrokesModule;
import me.eldodebug.confu.module.impl.ZoomModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.module.setting.Setting;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import me.eldodebug.confu.util.Keys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public final class MenuScreen extends Screen {

	private static final Identifier MARK = new Identifier(ConfuClient.MOD_ID, "textures/gui/mark.png");
	private static final int MARK_SOURCE = 256;
	private static final int RAIL_WIDTH = 60;
	private static final int ICON_SIZE = 36;
	private static final int PAD = 20;
	private static final int ROW = 28;
	private static final int CARD_RADIUS = 10;
	private static final int ROW_HEIGHT_MODULE = 56;
	private static final int CARD_GAP = 8;
	private static final int SWITCH_W = 34;
	private static final int SWITCH_H = 18;

	private enum Page {
		HOME, MODULES, SETTINGS
	}

	private record Hit(int x, int y, int w, int h, Runnable click, DoubleConsumer drag) {
		boolean contains(double mx, double my) {
			return mx >= x && mx < x + w && my >= y && my < y + h;
		}
	}

	private final List<Hit> hits = new ArrayList<>();
	private Page page = Page.HOME;
	private Category tab = Category.ALL;
	private Module detail;
	private Setting<?> capturing;
	private Hit dragging;
	private int scroll;

	private int px;
	private int py;
	private int pw;
	private int ph;

	public MenuScreen() {
		super(Text.literal("Confu Client"));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		hits.clear();
		renderBackground(context);

		pw = Math.min(width - 40, 780);
		ph = Math.min(height - 40, 470);
		px = (width - pw) / 2;
		py = (height - ph) / 2;

		Draw.roundRect(context, px, py, pw, ph, 14, Theme.PANEL);
		drawRail(context);

		int cx = px + RAIL_WIDTH + PAD;
		int cy = py + PAD;
		int cw = pw - RAIL_WIDTH - PAD * 2;
		int ch = ph - PAD * 2;

		switch (page) {
			case HOME -> drawHome(context, cx, cy, cw, ch);
			case MODULES -> drawModules(context, cx, cy, cw, ch);
			case SETTINGS -> drawSettings(context, cx, cy, cw, ch);
		}

		if (detail != null) {
			drawDetail(context, cx, cy, cw, ch);
		}
	}

	private void drawRail(DrawContext context) {
		Draw.roundRect(context, px, py, RAIL_WIDTH, ph, 14, Theme.RAIL);
		context.drawTexture(MARK, px + (RAIL_WIDTH - 32) / 2, py + 14, 32, 32, 0F, 0F, MARK_SOURCE, MARK_SOURCE, MARK_SOURCE, MARK_SOURCE);
		navButton(context, 0, Page.HOME);
		navButton(context, 1, Page.MODULES);
		navButton(context, 2, Page.SETTINGS);
	}

	private void navButton(DrawContext context, int index, Page target) {
		int x = px + (RAIL_WIDTH - ICON_SIZE) / 2;
		int y = py + 66 + index * (ICON_SIZE + 8);
		boolean active = page == target;
		Draw.roundRect(context, x, y, ICON_SIZE, ICON_SIZE, 10, active ? Theme.CARD_HOVER : Theme.RAIL);
		int color = active ? Theme.ACCENT : Theme.MUTED;
		int ix = x + ICON_SIZE / 2;
		int iy = y + ICON_SIZE / 2;
		switch (target) {
			case HOME -> {
					for (int row = 0; row < 7; row++) {
					context.fill(ix - 1 - row, iy - 7 + row, ix + 2 + row, iy - 6 + row, color);
				}
				context.fill(ix - 5, iy, ix + 5, iy + 7, color);
				context.fill(ix - 2, iy + 2, ix + 2, iy + 7, active ? Theme.CARD_HOVER : Theme.RAIL);
			}
			case MODULES -> {
				Draw.roundRect(context, ix - 7, iy - 7, 6, 6, 2, color);
				Draw.roundRect(context, ix + 1, iy - 7, 6, 6, 2, color);
				Draw.roundRect(context, ix - 7, iy + 1, 6, 6, 2, color);
				Draw.roundRect(context, ix + 1, iy + 1, 6, 6, 2, color);
			}
			case SETTINGS -> {
				Draw.roundRect(context, ix - 7, iy - 7, 14, 14, 7, color);
				Draw.roundRect(context, ix - 3, iy - 3, 6, 6, 3, active ? Theme.CARD_HOVER : Theme.RAIL);
			}
		}
		hit(x, y, ICON_SIZE, ICON_SIZE, () -> {
			page = target;
			detail = null;
			scroll = 0;
		});
	}

	private void drawHome(DrawContext context, int x, int y, int w, int h) {
		context.drawTexture(MARK, x, y, 40, 40, 0F, 0F, MARK_SOURCE, MARK_SOURCE, MARK_SOURCE, MARK_SOURCE);
		title(context, Lang.get("menu.title"), x + 52, y + 4, 1.4F);
		text(context, Lang.format("menu.subtitle", client.getGameVersion(), ConfuClient.get().version()), x + 52, y + 24, Theme.MUTED);

		int top = y + 56;
		int leftW = w * 55 / 100;
		int rightX = x + leftW + 14;
		int rightW = w - leftW - 14;

		card(context, x, top, leftW, h - 56);
		text(context, Lang.get("menu.home.quick"), x + 16, top + 14, Theme.TEXT);
		int rowY = top + 42;
		for (Module module : quickModules()) {
			quickToggle(context, x + 16, rowY, leftW - 32, module);
			rowY += 40;
		}

		int tipsH = (h - 56) * 6 / 10;
		card(context, rightX, top, rightW, tipsH);
		text(context, Lang.get("menu.home.tips"), rightX + 16, top + 14, Theme.TEXT);
		String[] tips = {
				Lang.get("menu.home.tip.menu"),
				Lang.get("menu.home.tip.zoom"),
				Lang.get("menu.home.tip.freelook"),
				Lang.get("menu.home.tip.crosshair"),
				Lang.get("menu.home.tip.keystrokes")
		};
		int tipY = top + 38;
		for (String tip : tips) {
			context.fill(rightX + 16, tipY + 4, rightX + 20, tipY + 8, Theme.ACCENT);
			for (String line : Draw.wrap(textRenderer, tip, rightW - 40)) {
				text(context, line, rightX + 28, tipY, Theme.MUTED);
				tipY += 11;
			}
			tipY += 6;
		}

		int buildY = top + tipsH + 14;
		card(context, rightX, buildY, rightW, h - 56 - tipsH - 14);
		text(context, Lang.get("menu.settings.about"), rightX + 16, buildY + 14, Theme.TEXT);
		int aboutY = buildY + 34;
		for (String line : Draw.wrap(textRenderer, Lang.get("menu.settings.credits"), rightW - 32)) {
			text(context, line, rightX + 16, aboutY, Theme.MUTED);
			aboutY += 11;
		}
		text(context, Lang.format("menu.settings.build", ConfuClient.get().version(), client.getGameVersion(), loaderVersion()),
				rightX + 16, aboutY + 6, Theme.MUTED);
	}

	private List<Module> quickModules() {
		return List.of(
				module(KeystrokesModule.class),
				module(ZoomModule.class),
				module(FreelookModule.class),
				module(CrosshairModule.class));
	}

	private void quickToggle(DrawContext context, int x, int y, int w, Module module) {
		text(context, module.name(), x, y + 4, Theme.TEXT);
		switchControl(context, x + w - SWITCH_W, y, module.isEnabled(), () -> toggleModule(module));
	}

	private void drawModules(DrawContext context, int x, int y, int w, int h) {
		int tabX = x;
		for (Category category : Category.values()) {
			String label = category.displayName();
			int tabW = textRenderer.getWidth(label) + 18;
			boolean active = tab == category;
			pill(context, tabX, y, tabW, 20, label, active, () -> {
				tab = category;
				scroll = 0;
			});
			tabX += tabW + 6;
		}

		List<Module> modules = ConfuClient.get().modules().inCategory(tab);
		int listY = y + 34;
		if (modules.isEmpty()) {
			text(context, Lang.get("menu.module.empty"), x, listY + 4, Theme.MUTED);
			return;
		}

		int colW = (w - CARD_GAP) / 2;
		int rows = (modules.size() + 1) / 2;
		int contentH = rows * (ROW_HEIGHT_MODULE + CARD_GAP);
		int viewH = h - 34;
		scroll = Math.max(0, Math.min(scroll, Math.max(0, contentH - viewH)));

		context.enableScissor(x, listY, x + w, listY + viewH);
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int col = i % 2;
			int row = i / 2;
			int cardX = x + col * (colW + CARD_GAP);
			int cardY = listY + row * (ROW_HEIGHT_MODULE + CARD_GAP) - scroll;
			if (cardY + ROW_HEIGHT_MODULE < listY || cardY > listY + viewH) {
				continue;
			}
			moduleCard(context, cardX, cardY, colW, module);
		}
		context.disableScissor();
	}

	private void moduleCard(DrawContext context, int x, int y, int w, Module module) {
		hit(x, y, w, ROW_HEIGHT_MODULE, () -> detail = module);
		Draw.roundRect(context, x, y, w, ROW_HEIGHT_MODULE, CARD_RADIUS, Theme.CARD);
		text(context, module.name(), x + 14, y + 12, Theme.TEXT);
		String description = module.description();
		int descWidth = w - 14 - SWITCH_W - 28;
		List<String> lines = Draw.wrap(textRenderer, description, descWidth);
		int lineY = y + 28;
		for (int i = 0; i < Math.min(2, lines.size()); i++) {
			String line = lines.get(i);
			if (i == 1 && lines.size() > 2) {
				line = line + "...";
			}
			text(context, line, x + 14, lineY, Theme.MUTED);
			lineY += 10;
		}
		switchControl(context, x + w - SWITCH_W - 12, y + (ROW_HEIGHT_MODULE - SWITCH_H) / 2, module.isEnabled(), () -> toggleModule(module));
	}

	private void drawSettings(DrawContext context, int x, int y, int w, int h) {
		title(context, Lang.get("menu.nav.settings"), x, y, 1.4F);

		int top = y + 36;
		card(context, x, top, w, 58);
		text(context, Lang.get("menu.settings.language"), x + 16, top + 12, Theme.TEXT);
		int buttonY = top + 30;
		pill(context, x + w - 16 - 150, buttonY, 70, 20, "English", Lang.current().equals(Lang.ENGLISH), () -> setLanguage(Lang.ENGLISH));
		pill(context, x + w - 16 - 74, buttonY, 74, 20, "Italiano", Lang.current().equals(Lang.ITALIAN), () -> setLanguage(Lang.ITALIAN));

		int keyY = top + 70;
		card(context, x, keyY, w, 58);
		text(context, Lang.get("menu.settings.menu_key"), x + 16, keyY + 12, Theme.TEXT);
		text(context, menuKeyName(), x + w - 16 - textRenderer.getWidth(menuKeyName()), keyY + 12, Theme.ACCENT);
		text(context, Lang.get("menu.settings.menu_key_hint"), x + 16, keyY + 32, Theme.MUTED);

		int aboutY = keyY + 70;
		card(context, x, aboutY, w, h - (aboutY - y));
		text(context, Lang.get("menu.settings.about"), x + 16, aboutY + 12, Theme.TEXT);
		text(context, Lang.format("menu.settings.build", ConfuClient.get().version(), client.getGameVersion(), loaderVersion()),
				x + 16, aboutY + 32, Theme.MUTED);
		int creditY = aboutY + 46;
		for (String line : Draw.wrap(textRenderer, Lang.get("menu.settings.credits"), w - 32)) {
			text(context, line, x + 16, creditY, Theme.MUTED);
			creditY += 11;
		}
	}

	private void drawDetail(DrawContext context, int x, int y, int w, int h) {
		int scrimX = px;
		int scrimY = py;
		Draw.roundRect(context, scrimX, scrimY, pw, ph, 14, Theme.SHADOW);
		hit(scrimX, scrimY, pw, ph, () -> { });

		List<Setting<?>> settings = detail.settings();
		int dialogW = Math.min(w, 420);
		int dialogX = x + (w - dialogW) / 2;
		int rows = settings.size() + 1;
		int dialogH = Math.min(h, 110 + rows * ROW + (detail instanceof CrosshairModule ? 110 : 0));
		int dialogY = y + Math.max(0, (h - dialogH) / 2);

		Draw.roundRect(context, dialogX, dialogY, dialogW, dialogH, CARD_RADIUS, Theme.CARD);

		int backW = textRenderer.getWidth(Lang.get("menu.module.back")) + 18;
		pill(context, dialogX + 14, dialogY + 14, backW, 20, Lang.get("menu.module.back"), false, () -> {
			detail = null;
			capturing = null;
		});
		text(context, detail.name(), dialogX + 14 + backW + 12, dialogY + 20, Theme.TEXT);
		text(context, detail.description(), dialogX + 14, dialogY + 42, Theme.MUTED);

		int rowY = dialogY + 66;
		text(context, Lang.get("menu.module.enabled"), dialogX + 14, rowY + 5, Theme.TEXT);
		switchControl(context, dialogX + dialogW - 14 - SWITCH_W, rowY + 1, detail.isEnabled(), () -> toggleModule(detail));
		rowY += ROW;

		for (Setting<?> setting : settings) {
			settingRow(context, dialogX + 14, rowY, dialogW - 28, setting);
			rowY += ROW;
		}

		if (detail instanceof CrosshairModule crosshair) {
			crosshair.drawPreview(context, dialogX + 14, rowY + 6, dialogW - 28, 88);
			hit(dialogX + 14, rowY + 6, dialogW - 28, 88, () -> { });
		}
	}

	private void settingRow(DrawContext context, int x, int y, int w, Setting<?> setting) {
		text(context, Lang.get(setting.nameKey()), x, y + 6, Theme.TEXT);
		int right = x + w;
		if (setting instanceof BooleanSetting bool) {
			switchControl(context, right - SWITCH_W, y + 4, bool.enabled(), () -> {
				bool.toggle();
				persist();
			});
		} else if (setting instanceof NumberSetting number) {
			String value = formatNumber(number);
			int sliderW = 120;
			int sliderX = right - sliderW;
			int valueW = textRenderer.getWidth(value);
			text(context, value, sliderX - 12 - valueW, y + 6, Theme.MUTED);
			slider(context, sliderX, y + 9, sliderW, number);
		} else if (setting instanceof ComboSetting combo) {
			String label = Lang.get("option." + combo.get());
			int pillW = textRenderer.getWidth(label) + 18;
			pill(context, right - pillW, y + 2, pillW, 20, label, true, () -> {
				combo.cycle();
				persist();
			});
		} else if (setting instanceof KeybindSetting keybind) {
			boolean waiting = capturing == keybind;
			String label = waiting ? Lang.get("menu.module.press_key") : Keys.name(keybind.code());
			int pillW = Math.max(90, textRenderer.getWidth(label) + 18);
			pill(context, right - pillW, y + 2, pillW, 20, label, waiting, () -> capturing = keybind);
		}
	}

	private void slider(DrawContext context, int x, int y, int w, NumberSetting number) {
		Draw.roundRect(context, x, y + 3, w, 4, 2, Theme.TRACK);
		int filled = Math.round((float) (number.ratio() * w));
		if (filled > 0) {
			Draw.roundRect(context, x, y + 3, Math.max(4, filled), 4, 2, Theme.ACCENT);
		}
		Draw.roundRect(context, x + filled - 5, y, 10, 10, 5, Theme.KNOB);
		hits.add(new Hit(x - 4, y - 6, w + 8, 22, () -> { }, mx -> number.setRatio((mx - x) / (double) w)));
	}

	private void switchControl(DrawContext context, int x, int y, boolean on, Runnable action) {
		Draw.roundRect(context, x, y, SWITCH_W, SWITCH_H, SWITCH_H / 2, on ? Theme.ACCENT : Theme.TRACK);
		int knob = SWITCH_H - 4;
		int knobX = on ? x + SWITCH_W - knob - 2 : x + 2;
		Draw.roundRect(context, knobX, y + 2, knob, knob, knob / 2, Theme.KNOB);
		hit(x, y, SWITCH_W, SWITCH_H, action);
	}

	private void pill(DrawContext context, int x, int y, int w, int h, String label, boolean active, Runnable action) {
		Draw.roundRect(context, x, y, w, h, h / 2, active ? Theme.ACCENT : Theme.CARD_HOVER);
		Draw.textCentered(context, textRenderer, label, x + w / 2, y + (h - 8) / 2, active ? Theme.ACCENT_TEXT : Theme.TEXT);
		hit(x, y, w, h, action);
	}

	private void card(DrawContext context, int x, int y, int w, int h) {
		Draw.roundRect(context, x, y, w, h, CARD_RADIUS, Theme.CARD);
	}

	private void title(DrawContext context, String text, int x, int y, float scale) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(scale, scale, 1F);
		context.drawText(textRenderer, text, 0, 0, Theme.TEXT, false);
		matrices.pop();
	}

	private void text(DrawContext context, String text, int x, int y, int color) {
		Draw.text(context, textRenderer, text, x, y, color);
	}

	private void hit(int x, int y, int w, int h, Runnable click) {
		hits.add(new Hit(x, y, w, h, click, null));
	}

	private void toggleModule(Module module) {
		module.toggle();
		persist();
	}

	private void setLanguage(String code) {
		Lang.setLanguage(code);
		ConfuClient.get().config().setLanguage(code);
		persist();
	}

	private void persist() {
		ConfuClient.get().saveConfig();
	}

	private String menuKeyName() {
		return Text.translatable(ConfuClient.get().menuKey().getBoundKeyTranslationKey()).getString();
	}

	private String formatNumber(NumberSetting number) {
		if (number.step() < 1D) {
			return String.format("%.2f", number.get());
		}
		return String.valueOf(number.getInt());
	}

	private String loaderVersion() {
		return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("fabricloader")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("?");
	}

	private <T extends Module> T module(Class<T> type) {
		return ConfuClient.get().modules().get(type);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return super.mouseClicked(mouseX, mouseY, button);
		}
		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);
			if (hit.contains(mouseX, mouseY)) {
				if (hit.drag() != null) {
					dragging = hit;
					hit.drag().accept(mouseX);
				}
				if (hit.click() != null) {
					hit.click().run();
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging != null && dragging.drag() != null) {
			dragging.drag().accept(mouseX);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging != null) {
			dragging = null;
			persist();
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (page == Page.MODULES && detail == null) {
			scroll -= (int) Math.round(amount * 18);
			scroll = Math.max(0, scroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (capturing instanceof KeybindSetting keybind) {
			keybind.set(keyCode == GLFW.GLFW_KEY_ESCAPE ? Keys.NONE : keyCode);
			capturing = null;
			persist();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && detail != null) {
			detail = null;
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		persist();
		super.close();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
