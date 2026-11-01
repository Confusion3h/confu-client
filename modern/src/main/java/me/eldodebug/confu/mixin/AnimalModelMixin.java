package me.eldodebug.confu.mixin;

import me.eldodebug.confu.render.FemaleGenderModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalModel.class)
public abstract class AnimalModelMixin {

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At("RETURN"))
	private void confu$renderFemaleGender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (this instanceof FemaleGenderModel model) {
			model.confu$renderFemaleGender(matrices, vertices, light, overlay, red, green, blue, alpha);
		}
	}
}
