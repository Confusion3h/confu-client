package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import net.minecraft.util.Identifier;

/** 1.8.9 SkinProtectMod: force Steve skin on local player. */
public final class SkinProtectModule extends Module {

	public static final Identifier STEVE = new Identifier("textures/entity/steve.png");

	public SkinProtectModule() {
		super("skin_protect", Category.PLAYER, "tt:SKIN_PROTECT", "tt:SKIN_PROTECT_DESCRIPTION");
	}
}
