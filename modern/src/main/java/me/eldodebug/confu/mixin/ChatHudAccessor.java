package me.eldodebug.confu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.hud.ChatHud;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {

	@Invoker("refresh")
	void confu$refresh();

	@Invoker("toChatLineY")
	double confu$toChatLineY(double y);

	@Invoker("getLineHeight")
	int confu$lineHeight();
}
