package me.eldodebug.confu.mixin;

import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

	@Shadow
	private ClientWorld world;

	@Inject(method = "onEntityStatus", at = @At("RETURN"))
	private void confu$damage(EntityStatusS2CPacket packet, CallbackInfo ci) {
		if (world == null || packet.getStatus() != 2) {
			return;
		}
		Entity entity = packet.getEntity(world);
		if (entity != null) {
			CombatHooks.fireDamage(entity);
		}
	}
}
