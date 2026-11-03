package me.eldodebug.confu.render;

import java.util.OptionalDouble;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/** Lines layer with a fixed width, so the Hitbox "line width" setting is honoured (vanilla lines pick their own width). */
public final class HitboxRenderLayers extends RenderLayer {

	private static final RenderLayer[] CACHE = new RenderLayer[11];

	private HitboxRenderLayers() {
		super(null, null, null, 0, false, false, null, null);
	}

	public static RenderLayer lines(float width) {
		int index = Math.max(0, Math.min(CACHE.length - 1, Math.round(width)));
		RenderLayer layer = CACHE[index];
		if (layer == null) {
			layer = RenderLayer.of("confu_hitbox_lines_" + index, VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256,
					RenderLayer.MultiPhaseParameters.builder()
							.program(LINES_PROGRAM)
							.lineWidth(new LineWidth(OptionalDouble.of(index)))
							.layering(VIEW_OFFSET_Z_LAYERING)
							.transparency(TRANSLUCENT_TRANSPARENCY)
							.target(ITEM_ENTITY_TARGET)
							.writeMaskState(COLOR_MASK)
							.cull(DISABLE_CULLING)
							.build(false));
			CACHE[index] = layer;
		}
		return layer;
	}
}
