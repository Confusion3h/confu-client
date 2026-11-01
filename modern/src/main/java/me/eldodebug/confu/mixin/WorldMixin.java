package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.TimeChangerModule;
import me.eldodebug.confu.module.impl.WeatherChangerModule;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

	@Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
	private void confu$timeChanger(CallbackInfoReturnable<Long> cir) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return;
		}
		try {
			long override = client.modules().get(TimeChangerModule.class).overrideTimeOfDay();
			if (override >= 0L) {
				cir.setReturnValue(override);
			}
		} catch (RuntimeException ignored) {
		}
	}

	@Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
	private void confu$rain(float delta, CallbackInfoReturnable<Float> cir) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return;
		}
		try {
			cir.setReturnValue(client.modules().get(WeatherChangerModule.class).rain(cir.getReturnValue()));
		} catch (RuntimeException ignored) {
		}
	}

	@Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true)
	private void confu$thunder(float delta, CallbackInfoReturnable<Float> cir) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return;
		}
		try {
			cir.setReturnValue(client.modules().get(WeatherChangerModule.class).thunder(cir.getReturnValue()));
		} catch (RuntimeException ignored) {
		}
	}
}
