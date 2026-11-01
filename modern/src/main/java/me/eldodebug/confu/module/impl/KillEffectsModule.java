package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;

/** 1.8.9 KillEffectsMod: themed burst when the hovered target disappears. */
public final class KillEffectsModule extends Module {

	private final BooleanSetting sound = bool("sound", "tt:SOUND", true);
	private final ComboSetting effect = combo("effect", "tt:EFFECT", "Blood", "Lighting", "Flames", "Cloud", "Blood");
	private final NumberSetting multiplier = number("multiplier", "tt:MULTIPLIER", 1, 1, 10, 1);

	private int targetId = -1;
	private double lastX;
	private double lastY;
	private double lastZ;
	private int entityId;
	private ClientWorld world;

	public KillEffectsModule() {
		super("kill_effects", Category.RENDER, "tt:KILL_EFFECTS", "tt:KILL_EFFECTS_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (mc.world != world) {
			world = mc.world;
			entityId = 0;
			targetId = -1;
		}
		if (mc.world == null || mc.player == null) {
			return;
		}

		if (mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
			targetId = living.getId();
			lastX = living.getX();
			lastY = living.getY();
			lastZ = living.getZ();
		}

		if (targetId < 0) {
			return;
		}
		Entity target = mc.world.getEntityById(targetId);
		if (target != null) {
			lastX = target.getX();
			lastY = target.getY();
			lastZ = target.getZ();
			return;
		}

		double dx = lastX - mc.player.getX();
		double dz = lastZ - mc.player.getZ();
		if (dx * dx + dz * dz < 100 && mc.player.age > 3) {
			playEffect();
		}
		targetId = -1;
	}

	private void playEffect() {
		switch (effect.get()) {
			case "Lighting" -> {
				LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
				lightning.setPosition(lastX, lastY, lastZ);
				lightning.setCosmetic(true);
				mc.world.addEntity(entityId--, lightning);
				playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
			}
			case "Flames" -> {
				spawn(ParticleTypes.FLAME, multiplier.getInt());
				playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.0F, 1.0F);
			}
			case "Cloud" -> {
				spawn(ParticleTypes.CLOUD, multiplier.getInt());
				playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0F, 1.0F);
			}
			default -> {
				spawn(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()), 50);
				playSound(SoundEvents.BLOCK_STONE_BREAK, 4.0F, 1.2F);
			}
		}
	}

	private void spawn(ParticleEffect particle, int count) {
		for (int i = 0; i < count; i++) {
			mc.world.addParticle(particle, lastX, lastY + 1.0, lastZ, 0, 0, 0);
		}
	}

	private void playSound(SoundEvent event, float volume, float pitch) {
		if (sound.enabled()) {
			mc.world.playSound(lastX, lastY, lastZ, event, SoundCategory.MASTER, volume, pitch, false);
		}
	}
}
