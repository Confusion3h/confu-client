package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.soar.compat.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/** The key that opens the mod menu: the vanilla "key.confu.menu" binding, saved in options.txt. */
public class MenuKeySetting extends KeybindSetting {

	public MenuKeySetting() {
		super(null, null);
	}

	@Override
	public int getKeyCode() {
		InputUtil.Key key = KeyBindingHelperAccess.bound(ConfuClient.get().menuKey());
		return key == null || key.equals(InputUtil.UNKNOWN_KEY) ? Keyboard.KEY_NONE : key.getCode();
	}

	@Override
	public void setKeyCode(int keyCode) {
		KeyBinding binding = ConfuClient.get().menuKey();
		binding.setBoundKey(keyCode == Keyboard.KEY_NONE ? InputUtil.UNKNOWN_KEY : InputUtil.Type.KEYSYM.createFromCode(keyCode));
		KeyBinding.updateKeysByCode();
		MinecraftClient.getInstance().options.write();
	}

	@Override
	public void reset() {
		setKeyCode(ConfuClient.get().menuKey().getDefaultKey().getCode());
	}

	@Override
	public String getName() {
		return "";
	}

	private static final class KeyBindingHelperAccess {
		static InputUtil.Key bound(KeyBinding binding) {
			return net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.getBoundKeyOf(binding);
		}
	}
}
