package me.eldodebug.confu.module.impl.waveycapes;

import me.eldodebug.confu.module.impl.WaveyCapesModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Per-player cape stick chain, ticked from the entity tick like the 1.8.9 EntityPlayer.onUpdate hook. */
public final class WaveyCapeSimulation {

	private final StickSimulation simulation = new StickSimulation();

	public StickSimulation simulation() {
		return simulation;
	}

	/** 1.8.9 IMixinEntityPlayer.updateSimulation — build the 16-point chain, then settle it. */
	public void updateSimulation(AbstractClientPlayerEntity player, int partCount) {

		boolean dirty = false;

		if (simulation.points.size() != partCount) {
			simulation.points.clear();
			simulation.sticks.clear();

			for (int i = 0; i < partCount; i++) {
				StickSimulation.Point point = new StickSimulation.Point();
				point.position.y = -i;
				point.locked = i == 0;
				simulation.points.add(point);
				if (i > 0) {
					simulation.sticks.add(new StickSimulation.Stick(simulation.points.get(i - 1), point, 1f));
				}
			}
			dirty = true;
		}

		if (dirty) {
			for (int i = 0; i < 10; i++) {
				simulate(player);
			}
		}
	}

	/** 1.8.9 IMixinEntityPlayer.simulate — chase the player position, multiplied by the height setting. */
	public void simulate(AbstractClientPlayerEntity player) {

		if (simulation.points.isEmpty()) {
			return;
		}

		simulation.points.get(0).prevPosition.copy(simulation.points.get(0).position);
		double d = player.capeX - player.getX();
		double m = player.capeZ - player.getZ();
		float n = player.prevBodyYaw + player.bodyYaw - player.prevBodyYaw;
		double o = Math.sin(n * 0.017453292F);
		double p = -Math.cos(n * 0.017453292F);
		WaveyCapesModule module = WaveyCapesModule.get();
		float heightMul = module == null ? 6 : module.heightMultiplier();
		double fallHack = MathHelper.clamp(simulation.points.get(0).position.y - (player.getY() * heightMul), 0d, 1d);

		simulation.points.get(0).position.x += (d * o + m * p) + fallHack;
		simulation.points.get(0).position.y = (float) (player.getY() * heightMul + (player.isInSneakingPose() ? -4 : 0));
		simulation.simulate();
	}
}
