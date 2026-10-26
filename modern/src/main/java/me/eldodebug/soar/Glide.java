package me.eldodebug.soar;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.Theme;
import me.eldodebug.soar.management.language.Language;
import me.eldodebug.soar.management.language.LanguageManager;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.fabricmc.loader.api.FabricLoader;

/**
 * The 1.8.9 client's service locator, kept under the same name so the GUI ported from 1.8.9
 * (mod menu, main menu, HUD editor) reads the same. Backed by the 1.20.1 module system.
 */
public final class Glide {

	private static final Glide INSTANCE = new Glide();

	private NanoVGManager nanoVGManager;
	private final ColorManager colorManager = new ColorManager();
	private LanguageManager languageManager;
	private ModManager modManager;
	private GuiModMenu modMenu;
	private boolean uiLoaded;

	private final me.eldodebug.soar.management.notification.NotificationManager notificationManager = new me.eldodebug.soar.management.notification.NotificationManager();
	private me.eldodebug.soar.management.music.MusicManager musicManager;

	public me.eldodebug.soar.management.notification.NotificationManager getNotificationManager() {
		return notificationManager;
	}

	/**
	 * Same folder as the 1.8.9 client: .confu/minecraft/glide/music (the installer puts the
	 * bundled tracks there). The 1.20.1 game dir is .confu/minecraft/modern-1.20.1.
	 */
	public me.eldodebug.soar.management.music.MusicManager getMusicManager() {
		if (musicManager == null) {
			java.io.File game = FabricLoader.getInstance().getGameDir().toFile();
			java.io.File shared = new java.io.File(game.getAbsoluteFile().getParentFile(), "glide/music");
			java.io.File dir = shared.isDirectory() ? shared : new java.io.File(game, "glide/music");
			dir.mkdirs();
			musicManager = new me.eldodebug.soar.management.music.MusicManager(dir);
		}
		return musicManager;
	}

	public static Glide getInstance() {
		return INSTANCE;
	}

	/** Created lazily on the render thread, once the GL context exists. */
	public NanoVGManager getNanoVGManager() {
		if (nanoVGManager == null) {
			nanoVGManager = new NanoVGManager();
		}
		return nanoVGManager;
	}

	public ColorManager getColorManager() {
		loadUi();
		return colorManager;
	}

	public LanguageManager getLanguageManager() {
		if (languageManager == null) {
			languageManager = new LanguageManager();
			Language saved = null;
			try {
				Path file = uiFile();
				if (Files.exists(file)) {
					JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
					if (json.has("language")) {
						String id = json.get("language").getAsString();
						for (Language l : Language.values()) {
							if (l.getId().equals(id)) {
								saved = l;
							}
						}
					}
				}
			} catch (Exception e) {
				ConfuClient.LOGGER.warn("Could not read UI language", e);
			}
			boolean italian = Lang.ITALIAN.equals(Lang.current());
			if (saved == null || (saved == Language.ITALIAN) != italian) {
				saved = italian ? Language.ITALIAN : Language.ENGLISH;
			}
			languageManager.setCurrentLanguage(saved);
		}
		return languageManager;
	}

	/** Keeps the 1.20.1 module texts (Lang) on the language picked in the 1.8.9 settings page. */
	public void onLanguageChanged(Language language) {
		saveUi();
		String code = language == Language.ITALIAN ? Lang.ITALIAN : Lang.ENGLISH;
		Lang.setLanguage(code);
		ConfuClient client = ConfuClient.get();
		if (client != null) {
			client.config().setLanguage(code);
			client.saveConfig();
		}
	}

	public ModManager getModManager() {
		if (modManager == null) {
			getLanguageManager();
			modManager = new ModManager(ConfuClient.get().modules());
		}
		return modManager;
	}

	public GuiModMenu getModMenu() {
		if (modMenu == null) {
			getLanguageManager();
			modMenu = new GuiModMenu();
		}
		return modMenu;
	}

	private static final me.eldodebug.soar.compat.ResourceLocation[] BACKGROUNDS = {
			new me.eldodebug.soar.compat.ResourceLocation("soar/mainmenu/background.png"),
			new me.eldodebug.soar.compat.ResourceLocation("soar/mainmenu/background-night.png"),
			new me.eldodebug.soar.compat.ResourceLocation("soar/mainmenu/background-dolphin.png"),
			new me.eldodebug.soar.compat.ResourceLocation("soar/mainmenu/background-unity.png"),
	};
	private int background;

	/** Main menu wallpaper: the four built-in 1.8.9 backgrounds, cycled by the image button. */
	public me.eldodebug.soar.compat.ResourceLocation getBackground() {
		loadUi();
		return BACKGROUNDS[Math.floorMod(background, BACKGROUNDS.length)];
	}

	public void nextBackground() {
		loadUi();
		background = (background + 1) % BACKGROUNDS.length;
		saveUi();
	}

	public String getVersion() {
		return ConfuClient.get() == null ? "dev" : ConfuClient.get().version();
	}

	public boolean isFirstLogin() {
		return false;
	}

	private Path uiFile() {
		return FabricLoader.getInstance().getConfigDir().resolve("confu-modern-ui.json");
	}

	private void loadUi() {
		if (uiLoaded) {
			return;
		}
		uiLoaded = true;
		try {
			Path file = uiFile();
			if (Files.exists(file)) {
				JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (json.has("theme")) {
					int id = json.get("theme").getAsInt();
					for (Theme t : Theme.values()) {
						if (t.getId() == id) {
							colorManager.setTheme(t);
						}
					}
				}
				if (json.has("background")) {
					background = json.get("background").getAsInt();
				}
				if (json.has("accent")) {
					colorManager.setCurrentColor(colorManager.getColorByName(json.get("accent").getAsString()));
				}
			}
		} catch (Exception e) {
			ConfuClient.LOGGER.warn("Could not read UI settings", e);
		}
	}

	/** Theme and accent colour, as the 1.8.9 profile saved them. */
	public void saveUi() {
		loadUi();
		try {
			JsonObject json = new JsonObject();
			json.addProperty("theme", colorManager.getTheme().getId());
			json.addProperty("accent", colorManager.getCurrentColor().getName());
			json.addProperty("background", background);
			if (languageManager != null) {
				json.addProperty("language", languageManager.getCurrentLanguage().getId());
			}
			Files.writeString(uiFile(), json.toString());
		} catch (Exception e) {
			ConfuClient.LOGGER.warn("Could not save UI settings", e);
		}
	}
}
