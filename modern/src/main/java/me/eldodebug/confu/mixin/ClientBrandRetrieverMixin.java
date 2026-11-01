package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ClientSpooferModule;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public abstract class ClientBrandRetrieverMixin {

	@Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true, remap = false)
	private static void confu$brand(CallbackInfoReturnable<String> cir) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return;
		}
		try {
			String brand = client.modules().get(ClientSpooferModule.class).brand();
			if (brand != null) {
				cir.setReturnValue(brand);
			}
		} catch (RuntimeException ignored) {
		}
	}
}
