package me.eldodebug.confu.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ChatTranslateModule;
import me.eldodebug.confu.util.Translator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** 1.8.9 MixinGuiChat + GuiChatHook: translate boxes and translated outgoing chat. */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

	protected ChatScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void confu$translateBoxes(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		ChatTranslateModule mod = confu$translate();
		if (mod == null || !mod.isEnabled()) {
			return;
		}

		boolean on = mod.outgoing();
		Text translateText = Text.literal("Translate: ")
				.append(Text.literal(on ? "On" : "Off").formatted(on ? Formatting.GREEN : Formatting.RED));
		String toText = "To: " + mod.targetLanguage();

		int y = this.height - 30;
		int translateWidth = this.textRenderer.getWidth(translateText);
		context.fill(2, y, 2 + translateWidth + 5, y + 14, Integer.MIN_VALUE);
		context.drawText(this.textRenderer, translateText, 5, y + 3, 0xFFFFFF, false);

		int toX = 2 + translateWidth + 10;
		int toWidth = this.textRenderer.getWidth(toText);
		context.fill(toX, y, toX + toWidth + 5, y + 14, Integer.MIN_VALUE);
		context.drawText(this.textRenderer, toText, toX + 3, y + 3, 0xFFFFFF, false);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void confu$translateClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		ChatTranslateModule mod = confu$translate();
		if (mod == null || !mod.isEnabled() || button != 0) {
			return;
		}

		boolean on = mod.outgoing();
		Text translateText = Text.literal("Translate: ")
				.append(Text.literal(on ? "On" : "Off").formatted(on ? Formatting.GREEN : Formatting.RED));
		String toText = "To: " + mod.targetLanguage();

		int y = this.height - 30;
		int translateWidth = this.textRenderer.getWidth(translateText);
		int toX = 2 + translateWidth + 10;
		int toWidth = this.textRenderer.getWidth(toText);

		if (mouseX >= 2 && mouseY >= y && mouseX <= 2 + translateWidth + 5 && mouseY <= y + 14) {
			mod.toggleOutgoing();
		}

		if (mouseX >= toX && mouseY >= y && mouseX <= toX + toWidth + 5 && mouseY <= y + 14) {
			mod.cycleTargetLanguage();
		}
	}

	@Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
	private void confu$sendTranslated(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
		ChatTranslateModule mod = confu$translate();
		if (mod == null || !mod.isEnabled() || !mod.outgoing()) {
			return;
		}

		String message = chatText.trim();
		if (message.isEmpty() || message.startsWith("/") || message.startsWith(".soarcmd")) {
			return;
		}

		cir.setReturnValue(true);

		CompletableFuture.runAsync(() -> {
			String translated = message;
			try {
				translated = Translator.translate(message, Translator.AUTO_DETECT, mod.targetLanguage());
			} catch (Exception e) {
				ConfuClient.LOGGER.warn("Translation failed", e);
			}

			String result = translated;
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.execute(() -> {
				if (mc.player == null) {
					return;
				}
				if (addToHistory) {
					mc.inGameHud.getChatHud().addToMessageHistory(result);
				}
				if (result.startsWith("/")) {
					mc.player.networkHandler.sendChatCommand(result.substring(1));
				} else {
					mc.player.networkHandler.sendChatMessage(result);
				}
			});
		});
	}

	private static ChatTranslateModule confu$translate() {
		try {
			return ConfuClient.get().modules().get(ChatTranslateModule.class);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
