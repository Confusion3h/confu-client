package me.eldodebug.soar;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import me.eldodebug.soar.injection.mixin.GlideTweaker;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.changelog.ChangelogManager;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.command.CommandManager;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.LanguageManager;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
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

public class Glide {

	private static Glide instance = new Glide();
	private Minecraft mc = Minecraft.getMinecraft();
	private GlideAPI api;
	
	private String name, version;
	
	private NanoVGManager nanoVGManager;
	private FileManager fileManager;
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
	private ChangelogManager changelogManager;
	private WaypointManager waypointManager;
	
	public Glide() {
		name = "Glide";
		version = "7.2";
	}
	
	public void start() {
		
		OptifineUtils.disableFastRender();
		this.removeOptifineZoom();
		
		fileManager = new FileManager();
		languageManager = new LanguageManager();
		eventManager = new EventManager();
		modManager = new ModManager();
		
		modManager.init();
		
		capeManager = new CapeManager();
		colorManager = new ColorManager();
		profileManager = new ProfileManager();
		
		api = new GlideAPI();
		api.init();
		
		commandManager = new CommandManager();
		screenshotManager = new ScreenshotManager();
		notificationManager = new NotificationManager();
		securityFeatureManager = new SecurityFeatureManager();
		quickPlayManager = new QuickPlayManager();
		changelogManager = new ChangelogManager();
		waypointManager = new WaypointManager();
		
		eventManager.register(new GlideHandler());
		
		setupLibraryPath();
		
		GlobalSettingsMod.getInstance().setToggled(true);
		mc.updateDisplay();
	}
	
	public void stop() {
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
	
    private void setupLibraryPath() {
    	
    	File cefDir = new File(fileManager.getExternalDir(), "cef");
    	
        try {
			System.setProperty("jcef.path", cefDir.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public static Glide getInstance() {
		return instance;
	}

	public GlideAPI getApi() {
		return api;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

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

	public ChangelogManager getChangelogManager() {
		return changelogManager;
	}

	public WaypointManager getWaypointManager() {
		return waypointManager;
	}
}
