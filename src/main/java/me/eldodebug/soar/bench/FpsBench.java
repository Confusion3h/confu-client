package me.eldodebug.soar.bench;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

/**
 * Same-scene FPS sample. Off unless {@code -Dconfu.fpsbench=<seconds>}.
 * Creates one superflat creative world and prints FPS_BENCH.
 */
public final class FpsBench {

	private FpsBench() {}

	public static void startIfRequested(final Minecraft mc) {
		final String raw = System.getProperty("confu.fpsbench", "");
		if (raw.isEmpty()) {
			return;
		}
		final int seconds;
		try {
			seconds = Math.max(5, Integer.parseInt(raw.trim()));
		} catch (NumberFormatException e) {
			System.out.println("FPS_BENCH failed bad seconds: " + raw);
			return;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					runBench(mc, seconds);
				} catch (Throwable e) {
					e.printStackTrace();
					System.out.println("FPS_BENCH failed " + e);
					System.exit(1);
				}
			}
		}, "confu-fps-bench");
		t.setDaemon(true);
		t.start();
	}

	private static void runBench(final Minecraft mc, final int seconds) throws Exception {
		mc.addScheduledTask(new Runnable() {
			public void run() {
				GameSettings gs = mc.gameSettings;
				gs.fancyGraphics = false;
				gs.clouds = 1;
				gs.ambientOcclusion = 1;
				gs.renderDistanceChunks = 8;
				gs.mipmapLevels = 4;
				gs.useVbo = true;
				gs.particleSetting = 1;
				gs.entityShadows = false;
				gs.enableVsync = false;
				gs.viewBobbing = false;
				gs.pauseOnLostFocus = false;
				gs.limitFramerate = (int) GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
				gs.fovSetting = 70f;
				Display.setVSyncEnabled(false);
				gs.saveOptions();
				// Default flat preset (bedrock, dirt, grass). This mappings set has no
				// WorldSettings.setGeneratorOptions; the stock flat world is the scene.
				WorldSettings ws = new WorldSettings(404L, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
				mc.launchIntegratedServer("fpsbench", "fpsbench", ws);
			}
		});
		long deadline = System.currentTimeMillis() + 120000L;
		while (System.currentTimeMillis() < deadline) {
			if (mc.theWorld != null && mc.thePlayer != null && mc.currentScreen == null) {
				break;
			}
			Thread.sleep(250L);
		}
		if (mc.theWorld == null || mc.thePlayer == null) {
			System.out.println("FPS_BENCH failed world did not load");
			System.exit(1);
		}
		Thread.sleep(12000L);
		long sum = 0;
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (int i = 0; i < seconds; i++) {
			Thread.sleep(1000L);
			int fps = Minecraft.getDebugFPS();
			samplesafe(i, fps);
			sum += fps;
			if (fps < min) {
				min = fps;
			}
			if (fps > max) {
				max = fps;
			}
		}
		long avg = sum / seconds;
		System.out.println("FPS_BENCH samples=" + seconds + " avg=" + avg + " min=" + min + " max=" + max);
		System.exit(0);
	}

	private static void samplesafe(int i, int fps) {
		System.out.println("FPS_BENCH sample " + (i + 1) + " " + fps);
	}
}
