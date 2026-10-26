package me.eldodebug.soar.management.mods;

import me.eldodebug.confu.hud.HudModule;
import me.eldodebug.soar.compat.Minecraft;
import me.eldodebug.soar.compat.ScaledResolution;

/** 1.8.9 HUDMod view (pixel position, size, drag state) of a 1.20.1 HUD module. */
public class HUDMod extends Mod {

	private final HudModule hud;
	private boolean dragging;
	private int draggingX, draggingY;

	public HUDMod(HudModule hud) {
		super(hud);
		this.hud = hud;
	}

	private ScaledResolution sr() {
		return new ScaledResolution(Minecraft.getMinecraft());
	}

	public int getX() {
		return Math.round(hud.x() * Math.max(0F, sr().getScaledWidth() - hud.scaledWidth()));
	}

	public int getY() {
		return Math.round(hud.y() * Math.max(0F, sr().getScaledHeight() - hud.scaledHeight()));
	}

	public void setX(int x) {
		float room = Math.max(1F, sr().getScaledWidth() - hud.scaledWidth());
		hud.setPosition(x / room, hud.y());
	}

	public void setY(int y) {
		float room = Math.max(1F, sr().getScaledHeight() - hud.scaledHeight());
		hud.setPosition(hud.x(), y / room);
	}

	public int getWidth() {
		return Math.round(hud.scaledWidth());
	}

	public int getHeight() {
		return Math.round(hud.scaledHeight());
	}

	public float getScale() {
		return hud.hudScale();
	}

	public void setScale(float scale) {
		hud.setHudScale(scale);
	}

	public boolean isDragging() {
		return dragging;
	}

	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	public int getDraggingX() {
		return draggingX;
	}

	public void setDraggingX(int draggingX) {
		this.draggingX = draggingX;
	}

	public int getDraggingY() {
		return draggingY;
	}

	public void setDraggingY(int draggingY) {
		this.draggingY = draggingY;
	}
}
