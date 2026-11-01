package me.eldodebug.confu.module.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.glfw.GLFW;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.util.Sound;
import net.minecraft.client.util.InputUtil;

/** 1.8.9 MechvibesMod — mechanical keyboard and mouse click sounds from the Soar sound packs. */
public final class MechvibesModule extends Module {

	private static final int[] SPECIAL_KEYS = {
			GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_CAPS_LOCK, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE,
			GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT};

	private final Sound mouseLeftSound = new Sound();
	private final Sound mouseRightSound = new Sound();
	private final Set<Integer> pressed = new HashSet<>();
	private volatile Map<Integer, Sound> keyMap = Map.of();

	private float tempKeyboardVolume;
	private String tempKeyboardMode;
	private float tempMouseVolume;
	private boolean mouseLeftPress;
	private boolean mouseRightPress;
	private volatile boolean loaded;

	private final BooleanSetting keyboard = bool("keyboard", "tt:KEYBOARD", true);
	private final ComboSetting keyType = combo("type", "tt:TYPE", "NK Cream",
			"NK Cream", "MX Blue", "MX Silver", "Razer Green", "HyperX Aqua", "MX Black", "Topre Purple");
	private final NumberSetting keyboardVolume = number("keyboard_volume", "tt:KEYBOARD_VOLUME", 0.5, 0, 1, 0.01);

	private final BooleanSetting mouse = bool("mouse", "tt:MOUSE", true);
	private final NumberSetting mouseVolume = number("mouse_volume", "tt:MOUSE_VOLUME", 0.5, 0, 1, 0.01);

	public MechvibesModule() {
		super("mechvibes", Category.OTHER, "tt:MECHVIBES", "tt:MECHVIBES_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		loaded = false;
		loadKeyboardSounds(folder(keyType.get()));
		loadMouseSounds();
	}

	@Override
	public void onTick() {
		if (!loaded) {
			return;
		}

		String mode = folder(keyType.get());
		if (!mode.equals(tempKeyboardMode)) {
			tempKeyboardMode = mode;
			loadKeyboardSounds(mode);
		}

		float keyboardVol = keyboardVolume.getFloat();
		if (tempKeyboardVolume != keyboardVol) {
			tempKeyboardVolume = keyboardVol;
			for (Sound sound : keyMap.values()) {
				sound.setVolume(keyboardVol);
			}
		}

		float mouseVol = mouseVolume.getFloat();
		if (tempMouseVolume != mouseVol) {
			tempMouseVolume = mouseVol;
			mouseLeftSound.setVolume(tempMouseVolume);
			mouseRightSound.setVolume(tempMouseVolume);
		}

		long window = mc.getWindow().getHandle();
		if (keyboard.enabled()) {
			Map<Integer, Sound> keys = keyMap;
			for (Map.Entry<Integer, Sound> entry : keys.entrySet()) {
				boolean down = InputUtil.isKeyPressed(window, entry.getKey());
				if (down && !pressed.contains(entry.getKey())) {
					pressed.add(entry.getKey());
					entry.getValue().play();
				} else if (!down) {
					pressed.remove(entry.getKey());
				}
			}
		}

		if (mouse.enabled()) {
			boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
			boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
			if (left && !mouseLeftPress) {
				mouseLeftPress = true;
				mouseLeftSound.play();
			}
			if (!left) {
				mouseLeftPress = false;
			}
			if (right && !mouseRightPress) {
				mouseRightPress = true;
				mouseRightSound.play();
			}
			if (!right) {
				mouseRightPress = false;
			}
		}
	}

	private static String folder(String option) {
		return switch (option) {
			case "MX Blue" -> "mx_blue";
			case "MX Silver" -> "mx_silver";
			case "Razer Green" -> "razer_green";
			case "HyperX Aqua" -> "hyperX_aqua";
			case "MX Black" -> "mx_black";
			case "Topre Purple" -> "topre_purple";
			default -> "nk_cream";
		};
	}

	private void loadKeyboardSounds(String type) {
		CompletableFuture.runAsync(() -> {
			Map<Integer, Sound> sounds = new HashMap<>();
			for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
				for (int special : SPECIAL_KEYS) {
					if (key == special) {
						sounds.put(key, clip(type, file(special), tempKeyboardVolume));
						break;
					}
				}
			}
			for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
				if (!sounds.containsKey(key)) {
					sounds.put(key, clip(type, String.valueOf(ThreadLocalRandom.current().nextInt(1, 6)), tempKeyboardVolume));
				}
			}
			keyMap = sounds;
		});
	}

	private static String file(int key) {
		return switch (key) {
			case GLFW.GLFW_KEY_TAB -> "tab";
			case GLFW.GLFW_KEY_BACKSPACE -> "backspace";
			case GLFW.GLFW_KEY_CAPS_LOCK -> "capslock";
			case GLFW.GLFW_KEY_ENTER -> "enter";
			case GLFW.GLFW_KEY_SPACE -> "space";
			case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "shift";
			default -> "1";
		};
	}

	private Sound clip(String type, String file, float volume) {
		Sound sound = new Sound();
		sound.loadClip(resource("soar/mechvibes/" + type + "/" + file + ".wav"));
		sound.setVolume(volume);
		return sound;
	}

	private void loadMouseSounds() {
		CompletableFuture.runAsync(() -> {
			mouseLeftSound.loadClip(resource("soar/mechvibes/mouse.wav"));
			mouseRightSound.loadClip(resource("soar/mechvibes/mouse.wav"));
			mouseLeftSound.setVolume(tempMouseVolume);
			mouseRightSound.setVolume(tempMouseVolume);
			loaded = true;
		});
	}

	private static InputStream resource(String location) {
		try {
			return Sound.class.getClassLoader().getResourceAsStream("assets/minecraft/" + location);
		} catch (Exception e) {
			return null;
		}
	}
}
