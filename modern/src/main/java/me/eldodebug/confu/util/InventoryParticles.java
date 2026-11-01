package me.eldodebug.confu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.InventoryModule;
import me.eldodebug.soar.utils.TimerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.RotationAxis;

/** 1.8.9 soar.ui.particle.ParticleEngine, drawn in GUI coordinates. */
public final class InventoryParticles {

	private static final Map<Screen, Engine> ENGINES = new WeakHashMap<>();

	private InventoryParticles() {
	}

	public static void render(DrawContext context, Screen screen) {
		InventoryModule mod = confu$inventory();
		if (mod == null || !mod.particle()) {
			return;
		}

		Engine engine = ENGINES.get(screen);
		if (engine == null || engine.width != screen.width || engine.height != screen.height) {
			engine = new Engine(screen.width, screen.height);
			ENGINES.put(screen, engine);
		}

		MinecraftClient mc = MinecraftClient.getInstance();
		double mouseX = mc.mouse.getX() * screen.width / Math.max(1, mc.getWindow().getWidth());
		double mouseY = mc.mouse.getY() * screen.height / Math.max(1, mc.getWindow().getHeight());
		engine.draw(context, (int) mouseX, (int) mouseY);
	}

	private static InventoryModule confu$inventory() {
		try {
			return ConfuClient.get().modules().get(InventoryModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static final class Engine {
		private final List<Particle> particles = new ArrayList<>();
		private final int width;
		private final int height;

		private Engine(int width, int height) {
			this.width = width;
			this.height = height;
			int amount = (width + height) / 8;
			Random random = new Random();
			for (int i = 0; i < amount; i++) {
				particles.add(new Particle(random.nextInt(Math.max(1, width)), random.nextInt(Math.max(1, height))));
			}
		}

		private void draw(DrawContext context, int mouseX, int mouseY) {
			for (Particle particle : particles) {
				if (particle.timer.delay(1000 / 60)) {
					particle.fall(width, height);
					particle.interpolation();
					particle.timer.reset();
				}

				int range = 50;
				boolean mouseOver = mouseX >= particle.x - range && mouseY >= particle.y - range
						&& mouseX <= particle.x + range && mouseY <= particle.y + range;

				if (mouseOver) {
					for (Particle other : particles) {
						if (other.x > particle.x && other.x - particle.x < range
								&& (other.y > particle.y && other.y - particle.y < range
										|| particle.y > other.y && particle.y - other.y < range)) {
							connect(context, particle.x, particle.y, other.x, other.y);
						}
					}
				}

				context.fill((int) particle.x, (int) particle.y,
						(int) particle.x + Math.max(1, (int) particle.size),
						(int) particle.y + Math.max(1, (int) particle.size), 0xFFFFFFFF);
			}
		}

		private void connect(DrawContext context, float xOne, float yOne, float xTwo, float yTwo) {
			float dx = xTwo - xOne;
			float dy = yTwo - yOne;
			float length = (float) Math.sqrt(dx * dx + dy * dy);
			if (length <= 0.01F) {
				return;
			}
			float angle = (float) Math.atan2(dy, dx);
			context.getMatrices().push();
			context.getMatrices().translate(xOne, yOne, 0);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation(angle));
			context.fill(0, 0, Math.max(1, (int) Math.ceil(length)), 1, 0xCCFFFFFF);
			context.getMatrices().pop();
		}
	}

	private static final class Particle {
		private float x;
		private float y;
		private final float size;
		private final float ySpeed;
		private final float xSpeed;
		private final TimerUtils timer = new TimerUtils();

		private Particle(int x, int y) {
			this.x = x;
			this.y = y;
			Random random = new Random();
			this.size = genRandom() + 0.4F;
			this.ySpeed = random.nextInt(5);
			this.xSpeed = random.nextInt(5);
		}

		private static float genRandom() {
			return (float) (0.3f + Math.random() * (0.6f - 0.3f + 1.0F));
		}

		private void interpolation() {
			for (int n = 0; n <= 64; ++n) {
				float f = n / 64.0f;
				float p1 = 1.02F * (1.0f - f) + 1.0f * f;
				float p2 = 1.02F + f * (1.0f - 1.02F);
				if (p1 != p2) {
					y -= f;
					x -= f;
				}
			}
		}

		private void fall(int width, int height) {
			y = y + ySpeed;
			x = x + xSpeed;

			if (y > height) {
				y = 1;
			}
			if (x > width) {
				x = 1;
			}
			if (x < 1) {
				x = width;
			}
			if (y < 1) {
				y = height;
			}
		}
	}
}
