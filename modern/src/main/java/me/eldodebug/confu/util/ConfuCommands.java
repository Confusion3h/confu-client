package me.eldodebug.confu.util;

import java.util.concurrent.CompletableFuture;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.impl.ChatTranslateModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** 1.8.9 CommandManager: the .soarcmd prefix and its translate command. */
public final class ConfuCommands {

	private ConfuCommands() {
	}

	/** Returns true when the line was a client command and must not reach the server. */
	public static boolean handle(String message) {
		if (!message.startsWith(".soarcmd")) {
			return false;
		}
		String[] args = message.split(" ", 3);
		if (args.length >= 2 && "translate".equals(args[1])) {
			translate(args.length >= 3 ? args[2] : "");
		}
		return true;
	}

	private static void translate(String text) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (text.isBlank() || ConfuClient.get() == null) {
			return;
		}
		ChatTranslateModule module = ConfuClient.get().modules().get(ChatTranslateModule.class);
		String to = switch (module.language()) {
			case "english" -> Translator.ENGLISH;
			case "chinese" -> Translator.CHINESE_SIMPLIFIED;
			case "polish" -> Translator.POLISH;
			default -> Translator.JAPANESE;
		};
		CompletableFuture.runAsync(() -> {
			try {
				String result = Translator.translate(text, Translator.AUTO_DETECT, to);
				mc.execute(() -> post(Text.literal("[Translate] ").formatted(Formatting.GREEN).append(Text.literal(result))));
			} catch (Exception e) {
				ConfuClient.LOGGER.warn("Translation failed", e);
			}
		});
	}

	private static void post(Text text) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.inGameHud == null) {
			return;
		}
		ChatTranslateModule.setIconSuppressed(true);
		try {
			mc.inGameHud.getChatHud().addMessage(text);
		} finally {
			ChatTranslateModule.setIconSuppressed(false);
		}
	}
}
