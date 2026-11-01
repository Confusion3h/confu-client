package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/**
 * 1.8.9 DamageTiltMod: restores directional hurt tilt from knockback.
 * On 1.20.1 vanilla already applies damage tilt; module kept for menu parity / future tweak.
 */
public final class DamageTiltModule extends Module {

	public DamageTiltModule() {
		super("damage_tilt", Category.PLAYER, "tt:DAMAGE_TILT", "tt:DAMAGE_TILT_DESCRIPTION");
	}
}
