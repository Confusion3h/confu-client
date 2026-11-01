package me.eldodebug.confu.mixin;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.AnimationsModule;
import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void confu$attack(PlayerEntity player, Entity target, CallbackInfo ci) {
		CombatHooks.fireAttack(target);
	}

	/** 1.8.9 Animations: block-hit while pushing stops held-block hitting so the arm swings. */
	@Inject(method = "isBreakingBlock", at = @At("HEAD"), cancellable = true)
	private void confu$oldBlockHit(CallbackInfoReturnable<Boolean> cir) {
		ConfuClient client = ConfuClient.get();
		if (client == null || client.modules() == null) {
			return;
		}
		try {
			AnimationsModule mod = client.modules().get(AnimationsModule.class);
			if (mod.blockHit() && mod.pushing()) {
				cir.setReturnValue(false);
			}
		} catch (RuntimeException ignored) {
		}
	}
}
