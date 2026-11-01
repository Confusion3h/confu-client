package me.eldodebug.confu.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/** Implemented by PlayerEntityModel so the extra chest part can be drawn without an entity in render. */
public interface FemaleGenderModel {

	void confu$setFemaleGender(boolean active, boolean sneaking);

	void confu$renderFemaleGender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha);
}
