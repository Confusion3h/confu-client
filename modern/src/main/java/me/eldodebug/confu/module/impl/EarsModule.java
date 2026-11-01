package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import net.minecraft.util.Identifier;

/** 1.8.9 EarsMod: custom black cat ears drawn on the local player. */
public final class EarsModule extends Module {

	public static final Identifier LEFT_TEXTURE = new Identifier("minecraft", "soar/cosmetics/ears/blackcatleft.png");
	public static final Identifier RIGHT_TEXTURE = new Identifier("minecraft", "soar/cosmetics/ears/blackcatright.png");

	public EarsModule() {
		super("ears", Category.PLAYER, "tt:EARS", "tt:EARS_DESCRIPTION");
	}
}
