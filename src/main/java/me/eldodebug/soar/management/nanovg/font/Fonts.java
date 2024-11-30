package me.eldodebug.soar.management.nanovg.font;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.util.ResourceLocation;

public class Fonts {
	
	private static final String PATH = "soar/fonts/";

	public static final Font FALLBACK = new Font("fallback", new ResourceLocation(PATH + "fallback.ttf"));
	public static final Font REGULAR = new Font("regular", new ResourceLocation(PATH + "Inter-Regular.ttf"));
	public static final Font MEDIUM = new Font("medium", new ResourceLocation(PATH + "Inter-Medium.ttf"));
	public static final Font DEMIBOLD = new Font("demi-bold", new ResourceLocation(PATH + "Inter-SemiBold.ttf"));
	public static final Font ICON = new Font("icon", new ResourceLocation(PATH + "Icon.ttf"));

	public static ArrayList<Font> getFonts() {
		return new ArrayList<Font>(Arrays.asList(MEDIUM, DEMIBOLD, REGULAR, ICON));
	}
}
