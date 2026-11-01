package me.eldodebug.confu.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

	@Invoker("hasLabel")
	boolean confu$hasLabel(Entity entity);

	@Invoker("renderLabelIfPresent")
	void confu$renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
}
