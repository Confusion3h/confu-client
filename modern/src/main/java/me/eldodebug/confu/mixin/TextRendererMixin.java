package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.NameProtectModule;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

	@ModifyVariable(
			method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
			at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private String confu$protectString(String text) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return text;
		}
		try {
			return client.modules().get(NameProtectModule.class).protect(text);
		} catch (RuntimeException e) {
			return text;
		}
	}
}
