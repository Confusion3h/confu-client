package me.eldodebug.soar;

import java.io.File;
import java.util.Arrays;

import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.management.mods.RestrictedMod;
import me.eldodebug.soar.ui.ClickEffects;
import me.eldodebug.soar.utils.Sound;
import org.apache.commons.lang3.ArrayUtils;

import me.eldodebug.soar.injection.mixin.GlideTweaker;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.command.CommandManager;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.LanguageManager;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.impl.InternalSettingsMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.notification.NotificationManager;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.management.quickplay.QuickPlayManager;
import me.eldodebug.soar.management.screenshot.ScreenshotManager;
import me.eldodebug.soar.management.security.SecurityFeatureManager;
import me.eldodebug.soar.management.waypoint.WaypointManager;
import me.eldodebug.soar.utils.OptifineUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.opengl.Display;

public class Glide {

	private static Glide instance = new Glide();
	private Minecraft mc = Minecraft.getMinecraft();
	private String name, version;
	private int verIdentifier;
	
	private NanoVGManager nanoVGManager;
	private FileManager fileManager;
	private me.eldodebug.soar.management.music.MusicManager musicManager;
	private LanguageManager languageManager;
	private EventManager eventManager;
	private ModManager modManager;
	private CapeManager capeManager;
	private ColorManager colorManager;
	private ProfileManager profileManager;
	private CommandManager commandManager;
	private ScreenshotManager screenshotManager;
	private NotificationManager notificationManager;
	private SecurityFeatureManager securityFeatureManager;
	private QuickPlayManager quickPlayManager;
    private WaypointManager waypointManager;
	private GuiModMenu modMenu;
	private GuiGlideMainMenu mainMenu;
	private long launchTime;
	private File firstLoginFile;
	private ClickEffects clickEffects;
	private RestrictedMod restrictedMod;
	
	public Glide() {
		name = "Confu Client";
		version = "7.2";
		verIdentifier = 7201;
	}
	
	public void start() {
		try {
			OptifineUtils.disableFastRender();
			this.removeOptifineZoom();
		} catch(Exception ignored) {}
		restrictedMod = new RestrictedMod();
		try {
			restrictedMod.shouldCheck = !System.getProperty("confusione.blacklistchecks", "true").equalsIgnoreCase("false");
		} catch (Exception ignored) {}
		fileManager = new FileManager();
		musicManager = new me.eldodebug.soar.management.music.MusicManager(fileManager.getMusicDir());
		firstLoginFile = new File(fileManager.getCacheDir(), "first.tmp");
		languageManager = new LanguageManager();
		eventManager = new EventManager();
		modManager = new ModManager();
		
		modManager.init();
		
		capeManager = new CapeManager();
		colorManager = new ColorManager();
		profileManager = new ProfileManager();

		modMenu = new GuiModMenu();
		mainMenu = new GuiGlideMainMenu();
		launchTime = System.currentTimeMillis();

		commandManager = new CommandManager();
		screenshotManager = new ScreenshotManager();
		notificationManager = new NotificationManager();
		securityFeatureManager = new SecurityFeatureManager();
		quickPlayManager = new QuickPlayManager();
		waypointManager = new WaypointManager();

		eventManager.register(new GlideHandler());

		InternalSettingsMod.getInstance().setToggled(true);
		applyFpsPresetOnce();
		clickEffects = new ClickEffects();
		mc.updateDisplay();
		me.eldodebug.soar.bench.FpsBench.startIfRequested(mc);
	}
	
	public void stop() {
		profileManager.save();
		Sound.play("soar/audio/close.wav", true);

	}
	
	/**
	 * First launch only (marker file in cache dir): when FPS mode is on, pick cheap
	 * vanilla video settings once. Later launches never touch the user's options.
	 */
	private void applyFpsPresetOnce() {
		File marker = new File(fileManager.getCacheDir(), "fpsmode-preset.tmp");

		if (marker.exists()) {
			return;
		}

		fileManager.createFile(marker);

		if (!InternalSettingsMod.isFpsMode()) {
			return;
		}

		// FPS-related mods on by default: the counter HUD and the boost mod
		for (me.eldodebug.soar.management.mods.Mod m : modManager.getMods()) {
			if (m instanceof me.eldodebug.soar.management.mods.impl.FPSDisplayMod
					|| m instanceof me.eldodebug.soar.management.mods.impl.FPSBoostMod) {
				m.setToggled(true);
			}
		}

		GameSettings gs = mc.gameSettings;
		gs.fancyGraphics = false;       // fast leaves: the biggest single cost in forests
		gs.clouds = 1;                  // fast clouds: flat, nearly free, keeps the sky alive
		gs.ambientOcclusion = 1;        // smooth lighting "minimum": cheap, removes flat-shaded look
		gs.renderDistanceChunks = 8;    // 8 chunks: smooth on iGPUs, still plenty for PvP
		gs.mipmapLevels = 4;            // removes distant texture shimmer at ~no cost
		gs.useVbo = true;               // VBOs cut driver overhead on chunk drawing
		gs.particleSetting = 1;
		gs.entityShadows = false;
		gs.enableVsync = false;
		gs.limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
		Display.setVSyncEnabled(false);
		gs.saveOptions();
		profileManager.save();
	}

	private void removeOptifineZoom() {
		if(GlideTweaker.hasOptifine) {
			try {
				this.unregisterKeybind((KeyBinding) GameSettings.class.getField("ofKeyBindZoom").get(mc.gameSettings));
			} catch(Exception e) {
				GlideLogger.error("Failed to unregister zoom key", e);
			}
		}
	}
	
    private void unregisterKeybind(KeyBinding key) {
        if (Arrays.asList(mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings = ArrayUtils.remove(mc.gameSettings.keyBindings, Arrays.asList(mc.gameSettings.keyBindings).indexOf(key));
            key.setKeyCode(0);
        }
    }
    
	public static Glide getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {return version;}

	public int getVersionIdentifier() {return verIdentifier;}

	public FileManager getFileManager() {
		return fileManager;
	}

	public ModManager getModManager() {
		return modManager;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public NanoVGManager getNanoVGManager() {
		return nanoVGManager;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}

	public CapeManager getCapeManager() {
		return capeManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public ScreenshotManager getScreenshotManager() {
		return screenshotManager;
	}

	public void setNanoVGManager(NanoVGManager nanoVGManager) {
		this.nanoVGManager = nanoVGManager;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public SecurityFeatureManager getSecurityFeatureManager() {
		return securityFeatureManager;
	}

	public QuickPlayManager getQuickPlayManager() {
		return quickPlayManager;
	}



	public WaypointManager getWaypointManager() {
		return waypointManager;
	}

	public GuiModMenu getModMenu() {
		return modMenu;
	}

	public GuiGlideMainMenu getMainMenu() {
		return mainMenu;
	}

	public long getLaunchTime() {
		return launchTime;
	}

	public void createFirstLoginFile() {
		Glide.getInstance().getFileManager().createFile(firstLoginFile);
	}

	public boolean isFirstLogin() {return !firstLoginFile.exists();}




	public ClickEffects getClickEffects() {return clickEffects;}

	public RestrictedMod getRestrictedMod() { return restrictedMod; }

	public me.eldodebug.soar.management.music.MusicManager getMusicManager() {
		return musicManager;
	}
}
