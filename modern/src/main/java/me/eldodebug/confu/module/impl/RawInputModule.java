package me.eldodebug.confu.module.impl;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 RawInputMod: GLFW raw mouse motion while grabbed. */
public final class RawInputModule extends Module {

	public RawInputModule() {
		super("raw_input", Category.OTHER, "tt:RAW_INPUT", "tt:RAW_INPUT_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		apply(true);
	}

	@Override
	protected void onDisable() {
		apply(false);
	}

	@Override
	public void onTick() {
		if (isEnabled()) {
			apply(true);
		}
	}

	private void apply(boolean raw) {
		long handle = mc.getWindow().getHandle();
		if (GLFW.glfwRawMouseMotionSupported()) {
			GLFW.glfwSetInputMode(handle, GLFW.GLFW_RAW_MOUSE_MOTION, raw ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		}
	}
}
