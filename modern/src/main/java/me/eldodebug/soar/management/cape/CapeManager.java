package me.eldodebug.soar.management.cape;

import java.io.File;
import java.util.ArrayList;


import me.eldodebug.soar.Glide;
import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.notification.NotificationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import me.eldodebug.soar.compat.ResourceLocation;

public class CapeManager {

	private final MinecraftClient mc = MinecraftClient.getInstance();
	private final java.util.Map<Cape, Identifier> textures = new java.util.HashMap<>();
	private final File selectedFile;
	
	private ArrayList<Cape> capes = new ArrayList<Cape>();
	private Cape currentCape;
	
	public CapeManager() {
		
		Glide instance = Glide.getInstance();
		FileManager fileManager = instance.getFileManager();
		File customCapeDir = fileManager.getCustomCapeDir();
		File cacheDir = fileManager.getCapeCacheDir();
		selectedFile = new File(cacheDir, "selected.txt");
		
		capes.add(new NormalCape("None", null, null, CapeCategory.ALL));
		
		add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", CapeCategory.MINECON);
		add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", CapeCategory.MINECON);
		add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", CapeCategory.MINECON);
		add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", CapeCategory.MINECON);
		add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", CapeCategory.MINECON);

		add("Canada", "flag/canada-sample.png", "flag/canada.png", CapeCategory.FLAG);
		add("commonwealth", "flag/commonwealth-sample.png", "flag/commonwealth.png", CapeCategory.FLAG);
		add("England", "flag/england-sample.png", "flag/england.png", CapeCategory.FLAG);
		add("Europe", "flag/europe-sample.png", "flag/europe.png", CapeCategory.FLAG);
		add("France", "flag/france-sample.png", "flag/france.png", CapeCategory.FLAG);
		add("Germany", "flag/germany-sample.png", "flag/germany.png", CapeCategory.FLAG);
		add("India", "flag/india-sample.png", "flag/india.png", CapeCategory.FLAG);
		add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", CapeCategory.FLAG);
		add("Italy", "flag/italy-sample.png", "flag/italy.png", CapeCategory.FLAG);
		add("Japan", "flag/japan-sample.png", "flag/japan.png", CapeCategory.FLAG);
		add("Korea", "flag/korean-sample.png", "flag/korean.png", CapeCategory.FLAG);
		add("LGBT", "flag/lgbt-sample.png", "flag/lgbt.png", CapeCategory.FLAG);
		add("NATO", "flag/nato-sample.png", "flag/nato.png", CapeCategory.FLAG);
		add("Scotland", "flag/scotland-sample.png", "flag/scotland.png", CapeCategory.FLAG);
		add("Trans", "flag/trans-sample.png", "flag/trans.png", CapeCategory.FLAG);
		add("Ukraine", "flag/ukraine-sample.png", "flag/ukraine.png", CapeCategory.FLAG);
		add("UN", "flag/un-sample.png", "flag/un.png", CapeCategory.FLAG);
		add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", CapeCategory.FLAG);
		add("United States", "flag/united-states-sample.png", "flag/united-states.png", CapeCategory.FLAG);




		store = new CapeStore(customCapeDir, cacheDir);
		loadCustomCapes(false);

		for(Cape c : capes) {
			
			if(c instanceof NormalCape) {
				
				NormalCape cape = (NormalCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
			
			if(c instanceof CustomCape) {
				
				CustomCape cape = (CustomCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
		}
	}
	
	private void add(String name, String samplePath, String capePath, CapeCategory category) {
		
		String cosmeticPath = "soar/cosmetics/cape/";
		
		capes.add(new NormalCape(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + capePath), category));
	}
	
	// ---- custom capes ---------------------------------------------------------------

	private CapeStore store;
	private String resolvedFor;

	/** (Re)reads the custom-cape folder. Main thread only (creates GL textures). */
	public void loadCustomCapes(boolean notify) {
		for (Cape c : new ArrayList<Cape>(capes)) {
			if (c instanceof CustomCape) {
				release((CustomCape) c);
			}
		}
		final int[] bad = { 0 };
		for (CapeStore.Entry e : store.scan(new CapeStore.Problem() {
			public void report(File file, String message) {
				bad[0]++;
				GlideLogger.warn("Skipping custom cape " + file.getName() + ": " + message);
			}
		})) {
			register(e);
		}
		resolvedFor = null; // re-resolve the selected cape against the new list
		if (notify) {
			int n = 0;
			for (Cape c : capes) {
				if (c instanceof CustomCape) {
					n++;
				}
			}
			toast(n + " custom cape" + (n == 1 ? "" : "s") + (bad[0] > 0 ? ", " + bad[0] + " skipped (see log)" : ""),
					bad[0] > 0 ? NotificationType.WARNING : NotificationType.SUCCESS);
		}
	}

	private CustomCape register(CapeStore.Entry e) {
		String name = e.name;
		while (getCapeOrNull(name) != null) {
			name = name + " (custom)";
		}
		Identifier id = new Identifier("confu", "dynamic/cape_" + Integer.toHexString(e.file.getName().hashCode()));
		try {
			java.io.ByteArrayOutputStream png = new java.io.ByteArrayOutputStream();
			javax.imageio.ImageIO.write(e.image.texture, "png", png);
			NativeImage image = NativeImage.read(new java.io.ByteArrayInputStream(png.toByteArray()));
			mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(image));
		} catch (java.io.IOException ex) {
			GlideLogger.error("Could not upload custom cape " + e.file.getName(), ex);
		}
		CustomCape cape = new CustomCape(name, e, new ResourceLocation(id.getPath()), CapeCategory.CUSTOM);
		capes.add(cape);
		textures.put(cape, id);
		return cape;
	}

	private void release(CustomCape c) {
		capes.remove(c);
		Identifier id = textures.remove(c);
		if (id != null) {
			mc.getTextureManager().destroyTexture(id);
		}
		Glide.getInstance().getNanoVGManager().unloadImage(c.getSample());
	}

	/** Validates and imports a picked file. Safe from any thread; UI changes run on the main thread. */
	public void importCape(final File file) {
		final CapeStore.Entry entry;
		try {
			entry = store.importFile(file);
		} catch (CapeImage.Invalid e) {
			toast(e.getMessage(), NotificationType.ERROR);
			return;
		} catch (Exception e) {
			GlideLogger.error("Cape import failed", e);
			toast("Import failed: " + e.getMessage(), NotificationType.ERROR);
			return;
		}
		mc.execute(new Runnable() {
			public void run() {
				setCurrentCape(register(entry));
				toast("Cape \"" + entry.name + "\" added and equipped", NotificationType.SUCCESS);
			}
		});
	}

	/** Deletes a custom cape's file and preview; falls back to None if it was equipped. */
	public void removeCape(CustomCape cape) {
		boolean wasCurrent = cape.equals(getCurrentCape());
		release(cape);
		if (!store.remove(cape.getEntry())) {
			toast("Could not delete " + cape.getEntry().file.getName(), NotificationType.ERROR);
		} else {
			toast("Cape \"" + cape.getName() + "\" removed", NotificationType.INFO);
		}
		if (wasCurrent) {
			setCurrentCape(getCapeByName("None"));
		}
	}

	public File getCustomCapeDir() {
		return store.getDir();
	}

	private static void toast(String msg, NotificationType type) {
		Glide g = Glide.getInstance();
		if (g != null && g.getNotificationManager() != null) {
			g.getNotificationManager().post("Capes", msg, type);
		}
	}
	
	public ArrayList<Cape> getCapes() {
		return capes;
	}
	
	/**
	 * The saved name lives in the profile, which loads after this manager is built and
	 * can change on profile switch, so resolve lazily (one string compare per call).
	 */
	public Cape getCurrentCape() {
		String name = readSelected();
		if (currentCape == null || !name.equals(resolvedFor)) {
			resolvedFor = name;
			currentCape = getCapeByName(name);
		}
		return currentCape;
	}

	public void setCurrentCape(Cape currentCape) {
		this.currentCape = currentCape;
		this.resolvedFor = currentCape.getName();
		writeSelected(currentCape.getName());
	}

	public Cape getCapeOrNull(String name) {
		for(Cape c : capes) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public Cape getCapeByName(String name) {
		Cape c = getCapeOrNull(name);
		return c != null ? c : capes.get(0); // "None"
	}
	

	/** Texture the 1.20.1 cape renderer should use, or null for "None". */
	public Identifier getCurrentTexture() {
		Cape c = getCurrentCape();
		if (c == null || c.getCape() == null) {
			return null;
		}
		Identifier id = textures.get(c);
		return id != null ? id : new Identifier("minecraft", c.getCape().getResourcePath());
	}

	private String selectedCache;

	private String readSelected() {
		if (selectedCache == null) {
			try {
				selectedCache = selectedFile.isFile()
						? new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()), java.nio.charset.StandardCharsets.UTF_8).trim()
						: "None";
			} catch (java.io.IOException e) {
				selectedCache = "None";
			}
		}
		return selectedCache;
	}

	private void writeSelected(String name) {
		selectedCache = name;
		try {
			java.nio.file.Files.write(selectedFile.toPath(), name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		} catch (java.io.IOException e) {
			GlideLogger.error("Could not save selected cape", e);
		}
	}
}
