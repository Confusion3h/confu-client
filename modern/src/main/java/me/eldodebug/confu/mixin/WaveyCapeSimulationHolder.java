package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.waveycapes.WaveyCapeSimulation;

/** Gives every player its own 1.8.9 WaveyCapes stick chain. */
public interface WaveyCapeSimulationHolder {

	WaveyCapeSimulation confu$capeSimulation();
}
