package me.eldodebug.confu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;

/** Client hooks mirroring 1.8.9 EventAttackEntity / EventDamageEntity. */
public final class CombatHooks {

	private static final List<Consumer<Entity>> ATTACK = new ArrayList<>();
	private static final List<Consumer<Entity>> DAMAGE = new ArrayList<>();

	private CombatHooks() {
	}

	public static void onAttack(Consumer<Entity> listener) {
		ATTACK.add(listener);
	}

	public static void onDamage(Consumer<Entity> listener) {
		DAMAGE.add(listener);
	}

	public static void fireAttack(Entity entity) {
		for (Consumer<Entity> c : ATTACK) {
			c.accept(entity);
		}
	}

	public static void fireDamage(Entity entity) {
		for (Consumer<Entity> c : DAMAGE) {
			c.accept(entity);
		}
	}
}
