package me.eldodebug.confu.mixin;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.render.entity.LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

	@Invoker("addFeature")
	boolean confu$callAddFeature(FeatureRenderer<?, ?> feature);
}
