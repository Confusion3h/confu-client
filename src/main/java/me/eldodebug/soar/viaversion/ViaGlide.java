package me.eldodebug.soar.viaversion;

import java.io.File;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.viaversion.gui.AsyncVersionSlider;

public class ViaGlide {
	
    public final static int NATIVE_VERSION = 47;
    private static ViaGlide instance;

    public static void create() {
    	instance = new ViaGlide();
    }

    private AsyncVersionSlider asyncVersionSlider;

    public ViaGlide() {
    	
    	FileManager fileManager = Glide.getInstance().getFileManager();
    	
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File(fileManager.getGlideDir(), "ViaVersion")).nativeVersion(NATIVE_VERSION).onProtocolReload(comparableProtocolVersion -> {
            if (getAsyncVersionSlider() != null) {
                getAsyncVersionSlider().setVersion(comparableProtocolVersion.getVersion());
            }
        }).build();
    }

    public static ViaGlide getInstance() {
		return instance;
	}

	public void initAsyncSlider() {
        this.initAsyncSlider(5, 5, 110, 20);
    }

    public void initAsyncSlider(int x, int y, int width, int height) {
        asyncVersionSlider = new AsyncVersionSlider(-1, x, y, Math.max(width, 110), height);
    }

    public AsyncVersionSlider getAsyncVersionSlider() {
        return asyncVersionSlider;
    }
}
