package me.eldodebug.confu.util;

import java.util.Map;
import java.util.WeakHashMap;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.InventoryModule;
import me.eldodebug.soar.utils.animation.normal.easing.EaseBackIn;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

/** 1.8.9 InventoryMod animation transform (normal slide / EaseBackIn). */
public final class InventoryAnimation {

	private static final Map<Screen, State> STATES = new WeakHashMap<>();
	private static int depth;

	private InventoryAnimation() {
	}

	public static void push(DrawContext context, Screen screen) {
		InventoryModule mod = confu$inventory();
		if (mod == null || !mod.animation()) {
			return;
		}

		State state = STATES.get(screen);
		if (state == null || state.width != screen.width || state.height != screen.height) {
			state = new State(screen.width, screen.height);
			STATES.put(screen, state);
		}

		double valueX;
		double valueY;

		if (mod.backIn()) {
			if (state.backInX == null) {
				state.backInX = new EaseBackIn(380, screen.width, 2);
				state.backInY = new EaseBackIn(380, screen.height, 2);
			}
			valueX = state.backInX.getValue();
			valueY = state.backInY.getValue();
		} else {
			state.x.setAnimation(screen.width, 18);
			state.y.setAnimation(screen.height, 18);
			valueX = state.x.getValue();
			valueY = state.y.getValue();
		}

		double xmod = screen.width / 2.0 - valueX / 2.0;
		double ymod = screen.height / 2.0 - valueY / 2.0;

		context.getMatrices().push();
		context.getMatrices().translate(xmod, ymod, 0);
		context.getMatrices().scale((float) (valueX / screen.width), (float) (valueY / screen.height), 1F);
		depth++;
	}

	public static void pop(DrawContext context) {
		if (depth > 0) {
			context.getMatrices().pop();
			depth--;
		}
	}

	private static InventoryModule confu$inventory() {
		try {
			return ConfuClient.get().modules().get(InventoryModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static final class State {
		private final int width;
		private final int height;
		private final SimpleAnimation x;
		private final SimpleAnimation y;
		private EaseBackIn backInX;
		private EaseBackIn backInY;

		private State(int width, int height) {
			this.width = width;
			this.height = height;
			this.x = new SimpleAnimation(0.0F);
			this.y = new SimpleAnimation(0.0F);
		}
	}
}
