package me.eldodebug.confu.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public abstract class HudModule extends Module {

	private final float defaultX;
	private final float defaultY;
	private float x;
	private float y;

	protected HudModule(String id, Category category, String nameKey, String descriptionKey, float defaultX, float defaultY) {
		super(id, category, nameKey, descriptionKey);
		this.defaultX = defaultX;
		this.defaultY = defaultY;
		this.x = defaultX;
		this.y = defaultY;
	}

	/** Width in logical pixels at scale 1. */
	protected abstract int width();

	/** Height in logical pixels at scale 1. */
	protected abstract int height();

	/** Draws with the origin at the element's top left, in logical pixels. */
	protected abstract void draw(DrawContext context, float tickDelta);

	protected float scale() {
		return 1F;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public void setPosition(float x, float y) {
		this.x = Math.max(0F, Math.min(1F, x));
		this.y = Math.max(0F, Math.min(1F, y));
	}

	public void resetPosition() {
		setPosition(defaultX, defaultY);
	}

	public float scaledWidth() {
		return width() * scale();
	}

	public float scaledHeight() {
		return height() * scale();
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		float s = scale();
		int screenWidth = mc.getWindow().getScaledWidth();
		int screenHeight = mc.getWindow().getScaledHeight();
		int px = Math.round(x * Math.max(0F, screenWidth - scaledWidth()));
		int py = Math.round(y * Math.max(0F, screenHeight - scaledHeight()));

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(px, py, 0);
		matrices.scale(s, s, 1F);
		draw(context, tickDelta);
		matrices.pop();
	}

	@Override
	public void read(JsonObject json) {
		super.read(json);
		JsonElement hx = json.get("hud_x");
		JsonElement hy = json.get("hud_y");
		if (hx != null && hx.isJsonPrimitive() && hy != null && hy.isJsonPrimitive()) {
			setPosition(hx.getAsFloat(), hy.getAsFloat());
		}
	}

	@Override
	public void write(JsonObject json) {
		super.write(json);
		json.addProperty("hud_x", x);
		json.addProperty("hud_y", y);
	}
}
