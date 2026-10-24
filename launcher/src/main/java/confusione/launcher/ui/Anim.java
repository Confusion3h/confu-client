package confusione.launcher.ui;

import javax.swing.JComponent;

/**
 * A value that eases toward a target with critically-damped exponential smoothing
 * (frame-rate independent). Repaints its owner while moving.
 */
public final class Anim implements Animator.Tickable {

	private final JComponent owner;
	private final float speed;
	private float value;
	private float target;

	public Anim(JComponent owner, float initial, float speed) {
		this.owner = owner;
		this.value = initial;
		this.target = initial;
		this.speed = speed;
	}

	public float get() {
		return value;
	}

	public float target() {
		return target;
	}

	public void set(float t) {
		if (t != target) {
			target = t;
			Animator.wake(this);
		}
	}

	public void snap(float v) {
		value = target = v;
		owner.repaint();
	}

	public boolean tick(float dt) {
		value += (target - value) * (1f - (float) Math.exp(-speed * dt));
		boolean moving = Math.abs(target - value) > 0.0015f;
		if (!moving) {
			value = target;
		}
		owner.repaint();
		return moving;
	}
}
