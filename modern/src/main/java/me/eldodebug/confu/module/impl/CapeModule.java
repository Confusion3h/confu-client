package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;
import net.minecraft.util.Identifier;

/** Capes from the 1.8.9 Minecon and Flag sets. Cartoon, Misc and Soar are left out on purpose. */
public final class CapeModule extends Module {

	public static final String[] CAPES = {
			"minecon/2011", "minecon/2012", "minecon/2013", "minecon/2015", "minecon/2016",
			"flag/italy", "flag/canada", "flag/commonwealth", "flag/england", "flag/europe", "flag/france",
			"flag/germany", "flag/india", "flag/indonesia", "flag/japan", "flag/korean", "flag/lgbt", "flag/nato",
			"flag/scotland", "flag/trans", "flag/ukraine", "flag/un", "flag/united-kingdom", "flag/united-states"};

	private final ComboSetting cape = combo("cape", "setting.cape.cape", "minecon/2016", CAPES);

	public CapeModule() {
		super("cape", Category.PLAYER, "module.cape.name", "module.cape.desc");
	}

	public Identifier texture() {
		return new Identifier(ConfuClient.MOD_ID, "textures/cape/" + cape.get() + ".png");
	}
}
