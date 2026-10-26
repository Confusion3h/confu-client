package me.eldodebug.soar.management.mods;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;

/** 1.8.9 Mod view of a 1.20.1 module, for the ported mod menu. */
public class Mod {

	private final Module module;
	private final SimpleAnimation animation = new SimpleAnimation();

	public Mod(Module module) {
		this.module = module;
	}

	public Module getModule() {
		return module;
	}

	public String getName() {
		return module.name();
	}

	public String getDescription() {
		return module.description();
	}

	public String getNameKey() {
		return module.id();
	}

	public boolean isToggled() {
		return module.isEnabled();
	}

	public void toggle() {
		module.toggle();
		me.eldodebug.confu.ConfuClient.get().saveConfig();
	}

	public void setToggled(boolean toggled) {
		module.setEnabled(toggled);
		me.eldodebug.confu.ConfuClient.get().saveConfig();
	}

	public boolean isHide() {
		return false;
	}

	public Boolean isRestricted() {
		return false;
	}

	public Boolean getAllowed() {
		return true;
	}

	public ModCategory getCategory() {
		Category c = module.category();
		switch (c) {
			case PLAYER: return ModCategory.PLAYER;
			case RENDER: return ModCategory.RENDER;
			case HUD: return ModCategory.HUD;
			case WORLD: return ModCategory.WORLD;
			case OTHER: return ModCategory.OTHER;
			default: return ModCategory.ALL;
		}
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}
}
