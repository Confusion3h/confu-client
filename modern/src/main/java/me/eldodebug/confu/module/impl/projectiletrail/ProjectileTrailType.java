package me.eldodebug.confu.module.impl.projectiletrail;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public enum ProjectileTrailType {

	BLACK_SMOKE(ParticleTypes.SMOKE, 0.07F, 0.0F, 2),
	FIRE(ParticleTypes.FLAME, 0.1F, 0.0F, 1),
	GREEN_STAR(ParticleTypes.HAPPY_VILLAGER, 0.0F, 0.1F, 1),
	HEARTS(ParticleTypes.HEART, 0.0F, 0.2F, 1),
	MAGIC(ParticleTypes.WITCH, 1.0F, 0.0F, 2),
	MUSIC_NOTES(ParticleTypes.NOTE, 1.0F, 1.0F, 2),
	SLIME(ParticleTypes.ITEM_SLIME, 0.5F, 0.3F, 1),
	SPARK(ParticleTypes.FIREWORK, 0.05F, 0.0F, 1),
	SWIRL(ParticleTypes.EFFECT, 1.0F, 0.0F, 1),
	WHITE_SMOKE(ParticleTypes.ITEM_SNOWBALL, 0.07F, 0.0F, 2);

	public final ParticleEffect particle;
	public final float velocity;
	public final float translate;
	public final int count;

	private ProjectileTrailType(ParticleEffect particle, float velocity, float translate, int count) {
		this.particle = particle;
		this.velocity = velocity;
		this.translate = translate;
		this.count = count;
	}

	public static ProjectileTrailType getTypeByKey(String key) {

		for (ProjectileTrailType t : ProjectileTrailType.values()) {
			if (t.name().equals(key)) {
				return t;
			}
		}

		return ProjectileTrailType.HEARTS;
	}
}
