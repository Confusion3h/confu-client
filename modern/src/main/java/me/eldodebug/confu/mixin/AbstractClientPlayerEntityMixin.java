package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.CapeModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

	@Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
	private void confu$cape(CallbackInfoReturnable<Identifier> cir) {
		if ((Object) this != MinecraftClient.getInstance().player || ConfuClient.get() == null) {
			return;
		}
		CapeModule module = ConfuClient.get().modules().get(CapeModule.class);
		if (module.isEnabled()) {
			cir.setReturnValue(module.texture());
		}
	}

	@Inject(method = "canRenderCapeTexture", at = @At("HEAD"), cancellable = true)
	private void confu$canRender(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this == MinecraftClient.getInstance().player && ConfuClient.get() != null
				&& ConfuClient.get().modules().get(CapeModule.class).isEnabled()) {
			cir.setReturnValue(true);
		}
	}
}
