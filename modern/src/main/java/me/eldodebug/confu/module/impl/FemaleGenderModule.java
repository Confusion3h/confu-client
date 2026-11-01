package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 FemaleGenderMod — registered for menu parity; deepen hooks in follow-ups. */
public final class FemaleGenderModule extends Module {
	public FemaleGenderModule() {
		super("female_gender", Category.PLAYER, "tt:FEMALE_GENDER", "tt:FEMALE_GENDER_DESCRIPTION");
	}
}
