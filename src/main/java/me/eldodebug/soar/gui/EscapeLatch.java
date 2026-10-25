package me.eldodebug.soar.gui;

/**
 * The ESC press that opens Options is still in the keyboard queue. Extra
 * key-down events in that same tick must not reach {@code GuiScreen.keyTyped},
 * which would close the screen (or, if a main menu were opened with the world
 * still loaded, leave the player on the title screen).
 */
public final class EscapeLatch {

	public static boolean swallow;

	private EscapeLatch() {}
}
