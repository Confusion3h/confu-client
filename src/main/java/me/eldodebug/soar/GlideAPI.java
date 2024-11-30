package me.eldodebug.soar;

import java.io.File;

import me.eldodebug.soar.gui.mainmenu.GuiGlideMainMenu;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.management.file.FileManager;

public class GlideAPI {

	private long launchTime;
	private GuiModMenu modMenu;
	private GuiGlideMainMenu mainMenu;
	private File firstLoginFile;
	
	public GlideAPI() {
		
		FileManager fileManager = Glide.getInstance().getFileManager();
		
		firstLoginFile = new File(fileManager.getCacheDir(), "first.tmp");
	}
	
	public void init() {
		launchTime = System.currentTimeMillis();
		modMenu = new GuiModMenu();
		mainMenu = new GuiGlideMainMenu();
	}
	
	public boolean isSpecialUser() {
		return true;
	}
	
	public GuiModMenu getModMenu() {
		return modMenu;
	}

	public long getLaunchTime() {
		return launchTime;
	}

	public GuiGlideMainMenu getMainMenu() {
		return mainMenu;
	}

	public void createFirstLoginFile() {
		Glide.getInstance().getFileManager().createFile(firstLoginFile);
	}
	
	public boolean isFirstLogin() {
		return !firstLoginFile.exists();
	}
}
