package me.eldodebug.confu.mixin;

import java.util.List;
import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.confu.module.impl.ChatModule;
import me.eldodebug.confu.module.impl.ChatTranslateModule;
import me.eldodebug.confu.module.impl.MinemenModule;
import me.eldodebug.confu.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 MixinGuiNewChat: chat smooth scroll, heads, infinity, background, compact and translate icon. */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	private List<ChatHudLine> messages;

	@Shadow
	private int scrolledLines;

	@Shadow
	public abstract int getWidth();

	@Shadow
	public abstract double getChatScale();


	@Unique
	private float confu$percentComplete;
	@Unique
	private long confu$prevMillis = System.currentTimeMillis();
	@Unique
	private float confu$animationPercent = 1F;
	@Unique
	private int confu$newLines;
	@Unique
	private int confu$lineBeingDrawn;
	@Unique
	private String confu$compactText = "";
	@Unique
	private int confu$compactCount;

	@Unique
	private static ChatModule confu$chat() {
		try {
			return ConfuClient.get().modules().get(ChatModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Unique
	private static ChatTranslateModule confu$translate() {
		try {
			return ConfuClient.get().modules().get(ChatTranslateModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}

	@ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
			at = @At("HEAD"), argsOnly = true)
	private Text confu$translateIcon(Text message) {
		ChatTranslateModule mod = confu$translate();
		if (mod == null || !mod.isEnabled() || ChatTranslateModule.iconSuppressed() || message.getString().isBlank()) {
			return message;
		}
		String plain = message.getString();
		Style style = Style.EMPTY.withColor(Formatting.GREEN)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/soarcmd translate " + plain))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(Lang.get("tt:CLICK_TO_TRANSLATE"))));
		return message.copy().append(Text.literal(" [" + '\u270E' + "]").setStyle(style));
	}

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
			at = @At("HEAD"), cancellable = true)
	private void confu$beforeAdd(Text message, MessageSignatureData signature, int ticks,
			net.minecraft.client.gui.hud.MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
		if (refresh) {
			return;
		}

		ChatModule mod = confu$chat();
		if (mod != null && mod.smooth()) {
			int width = MathHelper.floor(this.getWidth() / this.getChatScale());
			confu$newLines = ChatMessages.breakRenderedChatMessageLines(message, width, client.textRenderer).size() - 1;
		} else {
			confu$newLines = 0;
		}
		confu$percentComplete = 0;

		MinemenModule minemen = null;
		try {
			minemen = ConfuClient.get().modules().get(MinemenModule.class);
		} catch (RuntimeException ignored) {
		}
		if (minemen != null && minemen.autoPlay() && ChatUtil.hasClickCommand(message, "/requeue") && client.player != null) {
			client.player.networkHandler.sendChatCommand("requeue");
		}

		if (mod == null || !mod.compact()) {
			confu$compactText = "";
			return;
		}

		String raw = message.getString();
		if (raw.equals(confu$compactText)) {
			confu$compactCount++;
			if (!messages.isEmpty()) {
				Text combined = message.copy().append(Text.literal(" [x" + confu$compactCount + "]").formatted(Formatting.WHITE));
				messages.set(0, new ChatHudLine(ticks, combined, signature, indicator));
				((ChatHudAccessor) this).confu$refresh();
				ci.cancel();
			}
		} else {
			confu$compactText = raw;
			confu$compactCount = 1;
		}
	}

	@ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
			constant = @org.spongepowered.asm.mixin.injection.Constant(intValue = 100), require = 2)
	private int confu$infinityLimit(int original) {
		ChatModule mod = confu$chat();
		return mod != null && mod.infinity() ? 10000 : original;
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void confu$updateAnimation(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
		confu$lineBeingDrawn = 0;
		long now = System.currentTimeMillis();
		long diff = now - confu$prevMillis;
		confu$prevMillis = now;

		ChatModule mod = confu$chat();
		if (mod != null && mod.smooth() && confu$percentComplete < 1F) {
			confu$percentComplete += (mod.smoothSpeed() / 1000F) * (float) diff;
		}
		confu$percentComplete = MathHelper.clamp(confu$percentComplete, 0F, 1F);
		float t = confu$percentComplete - 1F;
		confu$animationPercent = MathHelper.clamp(1F - t * t * t * t, 0F, 1F);
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0, shift = At.Shift.AFTER))
	private void confu$smoothTranslate(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
		ChatModule mod = confu$chat();
		float y = 0F;
		if (mod != null && mod.smooth() && scrolledLines == 0) {
			y += 9F - 9F * confu$animationPercent;
		}
		ChatTranslateModule translate = confu$translate();
		if (translate != null && translate.isEnabled() && client.currentScreen instanceof ChatScreen) {
			y -= 8F;
		}
		if (y != 0F) {
			context.getMatrices().translate(0F, y, 0F);
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0))
	private void confu$chatBackground(DrawContext context, int x1, int y1, int x2, int y2, int color) {
		ChatModule mod = confu$chat();
		if (mod == null || mod.background()) {
			context.fill(x1, y1, x2, y2, color);
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
	private int confu$drawLine(DrawContext context, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
		ChatModule mod = confu$chat();
		int alpha = color >>> 24;

		if (mod != null && mod.smooth() && confu$lineBeingDrawn <= confu$newLines) {
			alpha = (int) (alpha * confu$animationPercent);
		}

		if (mod != null && mod.head()) {
			Identifier skin = confu$skinFor(ChatUtil.plain(text));
			if (skin != null) {
				context.drawTexture(skin, x, y - 1, 8F, 8F, 8, 8, 64, 64);
				context.drawTexture(skin, x, y - 1, 40F, 8F, 8, 8, 64, 64);
				x += 10;
			}
		}

		int rgb = color & 0xFFFFFF;
		confu$lineBeingDrawn++;
		return context.drawTextWithShadow(textRenderer, text, x, y, (alpha << 24) | rgb);
	}

	@Redirect(method = "getTextStyleAt", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/ChatHud;toChatLineY(D)D"))
	private double confu$translateClickY(ChatHud hud, double y) {
		double lineY = ((ChatHudAccessor) hud).confu$toChatLineY(y);
		ChatTranslateModule translate = confu$translate();
		if (translate != null && translate.isEnabled() && client.currentScreen instanceof ChatScreen) {
			lineY -= 8.0 / ((ChatHudAccessor) hud).confu$lineHeight();
		}
		return lineY;
	}

	@Unique
	private Identifier confu$skinFor(String line) {
		int open = line.indexOf('<');
		int close = line.indexOf('>', open + 1);
		if (open < 0 || close < 0 || client.getNetworkHandler() == null) {
			return null;
		}
		String name = line.substring(open + 1, close).trim();
		for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
			if (entry.getProfile() != null && entry.getProfile().getName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
				return entry.getSkinTexture();
			}
		}
		return null;
	}
}
