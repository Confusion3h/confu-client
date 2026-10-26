package me.eldodebug.confu.render;

public final class Theme {

	public static final int PANEL = 0xF2111114;
	public static final int RAIL = 0xFF0C0C0E;
	public static final int CARD = 0xFF1A1A1E;
	public static final int CARD_HOVER = 0xFF222228;
	public static final int TRACK = 0xFF3A3A41;
	public static final int TEXT = 0xFFF2F2F4;
	public static final int MUTED = 0xFF9B9BA5;
	public static final int ACCENT = 0xFF6EE7A3;
	public static final int ACCENT_TEXT = 0xFF0B1A12;
	public static final int KNOB = 0xFFFFFFFF;
	public static final int SHADOW = 0x66000000;
	public static final int KEY = 0x8C000000;

	private Theme() {
	}

	public static int withAlpha(int color, float alpha) {
		int a = Math.round(((color >>> 24) & 0xFF) * Math.max(0F, Math.min(1F, alpha)));
		return (a << 24) | (color & 0x00FFFFFF);
	}

	public static int lerp(int from, int to, float t) {
		float k = Math.max(0F, Math.min(1F, t));
		int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * k);
		int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * k);
		int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * k);
		int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * k);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
