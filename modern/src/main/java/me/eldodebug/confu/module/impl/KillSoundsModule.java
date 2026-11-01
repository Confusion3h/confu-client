package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;

/** 1.8.9 KillSoundsMod: play a sound when a recently attacked entity dies. */
public final class KillSoundsModule extends Module {

	private final NumberSetting volume = number("volume", "tt:VOLUME", 0.5, 0, 1, 0.05);
	private int targetId = -1;

	public KillSoundsModule() {
		super("kill_sounds", Category.OTHER, "tt:KILL_SOUNDS", "tt:KILL_SOUNDS_DESCRIPTION");
		CombatHooks.onAttack(e -> { if (isEnabled()) targetId = e.getId(); });
	}

	@Override
	public void onTick() {
		if (mc.world == null || targetId < 0) return;
		var e = mc.world.getEntityById(targetId);
		if (e instanceof LivingEntity living && !living.isAlive()) {
			if (mc.player != null) {
				mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, volume.getFloat(), 1F);
			}
			targetId = -1;
		} else if (e == null) {
			targetId = -1;
		}
	}
}
