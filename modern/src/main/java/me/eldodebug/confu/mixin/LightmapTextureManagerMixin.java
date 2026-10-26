package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.eldodebug.confu.module.impl.FullbrightModule;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

	/** The second SimpleOption.getValue() in update() is options.getGamma().getValue(). */
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 1))
	private Object confu$fullbright(SimpleOption<?> option) {
		return FullbrightModule.active() ? (Object) Double.valueOf(20.0) : option.getValue();
	}
}
