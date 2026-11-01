package me.eldodebug.confu.module.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.impl.projectiletrail.ProjectileTrailType;
import me.eldodebug.confu.module.setting.ComboSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;

/** 1.8.9 ProjectileTrailMod: particles behind the player's own projectiles. */
public final class ProjectileTrailModule extends Module {

	private ProjectileTrailType type = ProjectileTrailType.HEARTS;

	private final ArrayList<ThrownEntity> throwables = new ArrayList<>();
	private int ticks;

	private final ComboSetting mode = combo("type", "tt:TYPE", "HEARTS",
			"BLACK_SMOKE", "FIRE", "GREEN_STAR", "HEARTS", "MAGIC", "MUSIC_NOTES",
			"SLIME", "SPARK", "SWIRL", "WHITE_SMOKE");

	public ProjectileTrailModule() {
		super("projectile_trail", Category.PLAYER, "tt:PROJECTILE_TRAIL", "tt:PROJECTILE_TRAIL_DESCRIPTION");
	}

	@Override
	public void onTick() {

		type = ProjectileTrailType.getTypeByKey(mode.get());
		ticks = ticks >= 20 ? 0 : ticks + 2;

		updateThrowables();

		if (mc.world == null || mc.player == null) {
			return;
		}

		for (Entity entity : mc.world.getEntities()) {
			if (entity != null && (isValidEntity(entity) || throwables.contains(entity)) && entity.distanceTo(mc.player) > 3.0F) {
				spawnParticle(type, entity.getPos());
			}
		}
	}

	private void spawnParticle(ProjectileTrailType trail, Vec3d vector) {
		if (trail == ProjectileTrailType.GREEN_STAR || trail == ProjectileTrailType.HEARTS) {
			if (ticks % 4 != 0) {
				return;
			}
		}
		if (trail == ProjectileTrailType.MUSIC_NOTES && ticks % 2 != 0) {
			return;
		}

		float translate = trail.translate;
		float velocity = trail.velocity;

		for (int i = 0; i < trail.count; ++i) {
			Random random = new Random();
			float x = random.nextFloat() * translate * 2.0F - translate;
			float y = random.nextFloat() * translate * 2.0F - translate;
			float z = random.nextFloat() * translate * 2.0F - translate;
			float xVel = random.nextFloat() * velocity * 2.0F - velocity;
			float yVel = random.nextFloat() * velocity * 2.0F - velocity;
			float zVel = random.nextFloat() * velocity * 2.0F - velocity;

			mc.world.addParticle(trail.particle, true,
					vector.x + x, vector.y + y, vector.z + z,
					xVel, yVel, zVel);
		}
	}

	private boolean isValidEntity(Entity entity) {
		if (entity.getX() == entity.prevX && entity.getY() == entity.prevY && entity.getZ() == entity.prevZ) {
			return false;
		}

		if (entity instanceof ArrowEntity || entity instanceof SpectralArrowEntity) {
			Entity owner = ((PersistentProjectileEntity) entity).getOwner();
			if (owner != null && owner.equals(mc.player)) {
				return true;
			}
		} else if (entity instanceof FishingBobberEntity bobber) {
			if (bobber.getPlayerOwner() != null && bobber.getPlayerOwner().equals(mc.player)) {
				return true;
			}
		} else if (entity instanceof ThrownEntity thrown && entity.age == 1
				&& entity.squaredDistanceTo(mc.player) <= 11.0D && !throwables.contains(entity)) {
			throwables.add(thrown);
			return true;
		}

		return false;
	}

	private void updateThrowables() {

		Iterator<ThrownEntity> iterator = throwables.iterator();

		while (iterator.hasNext()) {
			ThrownEntity throwable = iterator.next();

			if (throwable == null || throwable.isRemoved()) {
				iterator.remove();
			}
		}
	}
}
