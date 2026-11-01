package me.eldodebug.confu.module.impl;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 BorderlessFullscreenMod: undecorated fullscreen via GLFW. */
public final class BorderlessFullscreenModule extends Module {

	private int prevX, prevY, prevW, prevH;
	private boolean applied;

	public BorderlessFullscreenModule() {
		super("borderless_fullscreen", Category.OTHER, "tt:BORDERLESS_FULSCREEN", "tt:BORDERLESS_FULLSCREEN_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		apply(true);
	}

	@Override
	protected void onDisable() {
		apply(false);
	}

	private void apply(boolean borderless) {
		long handle = mc.getWindow().getHandle();
		if (borderless) {
			int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
			GLFW.glfwGetWindowPos(handle, x, y);
			GLFW.glfwGetWindowSize(handle, w, h);
			prevX = x[0];
			prevY = y[0];
			prevW = w[0];
			prevH = h[0];
			var monitor = GLFW.glfwGetPrimaryMonitor();
			var mode = GLFW.glfwGetVideoMode(monitor);
			if (mode != null) {
				GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
				GLFW.glfwSetWindowMonitor(handle, 0, 0, 0, mode.width(), mode.height(), mode.refreshRate());
				applied = true;
			}
		} else if (applied) {
			GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
			GLFW.glfwSetWindowMonitor(handle, 0, prevX, prevY, prevW, prevH, 0);
			applied = false;
		}
	}
}
