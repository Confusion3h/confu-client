package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.waveycapes.WaveyCapeSimulationHolder;
import me.eldodebug.confu.module.impl.skin3d.Skin3DLayersHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.CapeModule;
import me.eldodebug.confu.module.impl.SkinProtectModule;
import me.eldodebug.confu.module.impl.WaveyCapesModule;
import me.eldodebug.confu.module.impl.skin3d.PlayerSkinLayers;
import me.eldodebug.confu.module.impl.waveycapes.WaveyCapeSimulation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin implements WaveyCapeSimulationHolder, Skin3DLayersHolder {

	@Unique
	private WaveyCapeSimulation confu$capeSimulation;
	@Unique
	private PlayerSkinLayers confu$skinLayers;

	@Override
	public WaveyCapeSimulation confu$capeSimulation() {
		if (confu$capeSimulation == null) {
			confu$capeSimulation = new WaveyCapeSimulation();
		}
		return confu$capeSimulation;
	}

	@Override
	public PlayerSkinLayers confu$skinLayers() {
		if (confu$skinLayers == null) {
			confu$skinLayers = new PlayerSkinLayers();
		}
		return confu$skinLayers;
	}

	/** 1.8.9 MixinEntityPlayer.onUpdate — the cape chain chases the player every tick. */
	@Inject(method = "tick", at = @At("HEAD"))
	private void confu$capeTick(CallbackInfo ci) {
		if (ConfuClient.get() == null || WaveyCapesModule.get() == null) {
			return;
		}
		WaveyCapeSimulation simulation = confu$capeSimulation();
		if (!simulation.simulation().points.isEmpty()) {
			simulation.simulate((AbstractClientPlayerEntity) (Object) this);
		}
	}

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

	@Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
	private void confu$skinProtect(CallbackInfoReturnable<Identifier> cir) {
		if ((Object) this != MinecraftClient.getInstance().player || ConfuClient.get() == null) {
			return;
		}
		if (ConfuClient.get().modules().get(SkinProtectModule.class).isEnabled()) {
			cir.setReturnValue(SkinProtectModule.STEVE);
		}
	}
}
