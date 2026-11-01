package me.eldodebug.confu.mixin;

import me.eldodebug.confu.module.impl.PlayerPredicatorModule;
import me.eldodebug.confu.util.CombatHooks;
import me.eldodebug.confu.util.ConfuCommands;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

	@Shadow
	private ClientWorld world;

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void confu$clientCommandChat(String content, CallbackInfo ci) {
		if (ConfuCommands.handle(content)) {
			ci.cancel();
		}
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void confu$clientCommand(String command, CallbackInfoReturnable<Boolean> cir) {
		if (ConfuCommands.handle("." + command)) {
			cir.setReturnValue(true);
		}
	}

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

	@Inject(method = "onEntity", at = @At("RETURN"))
	private void confu$playerPredicatorMove(EntityS2CPacket packet, CallbackInfo ci) {
		if (world != null) {
			PlayerPredicatorModule.handleEntityPacket(packet, world);
		}
	}

	@Inject(method = "onEntityPosition", at = @At("RETURN"))
	private void confu$playerPredicatorTeleport(EntityPositionS2CPacket packet, CallbackInfo ci) {
		PlayerPredicatorModule.handleTeleportPacket(packet);
	}
}
