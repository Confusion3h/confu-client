package me.eldodebug.soar.compat;

import me.eldodebug.confu.util.Keys;
import org.lwjgl.glfw.GLFW;

/** LWJGL2 Keyboard names mapped to GLFW key codes; key codes passed around the ported GUI are GLFW codes. */
public final class Keyboard {

	public static final int KEY_NONE = Keys.NONE;
	public static final int KEY_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
	public static final int KEY_LCONTROL = GLFW.GLFW_KEY_LEFT_CONTROL;
	public static final int KEY_RCONTROL = GLFW.GLFW_KEY_RIGHT_CONTROL;
	public static final int KEY_LSHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
	public static final int KEY_RSHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
	public static final int KEY_BACK = GLFW.GLFW_KEY_BACKSPACE;
	public static final int KEY_DELETE = GLFW.GLFW_KEY_DELETE;
	public static final int KEY_LEFT = GLFW.GLFW_KEY_LEFT;
	public static final int KEY_RIGHT = GLFW.GLFW_KEY_RIGHT;
	public static final int KEY_UP = GLFW.GLFW_KEY_UP;
	public static final int KEY_DOWN = GLFW.GLFW_KEY_DOWN;
	public static final int KEY_HOME = GLFW.GLFW_KEY_HOME;
	public static final int KEY_END = GLFW.GLFW_KEY_END;
	public static final int KEY_RETURN = GLFW.GLFW_KEY_ENTER;
	public static final int KEY_A = GLFW.GLFW_KEY_A;
	public static final int KEY_C = GLFW.GLFW_KEY_C;
	public static final int KEY_F = GLFW.GLFW_KEY_F;
	public static final int KEY_V = GLFW.GLFW_KEY_V;
	public static final int KEY_X = GLFW.GLFW_KEY_X;

	private Keyboard() {
	}

	public static boolean isKeyDown(int code) {
		return Keys.isDown(code);
	}

	public static String getKeyName(int code) {
		return Keys.name(code);
	}
}
