package me.eldodebug.confu.util;

import java.util.Optional;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class ChatUtil {

	private ChatUtil() {
	}

	/** True when any run_command click event in the message starts with the prefix. */
	public static boolean hasClickCommand(Text message, String prefix) {
		Optional<Boolean> found = message.visit((style, string) -> {
			ClickEvent click = style.getClickEvent();
			if (click != null && click.getAction() == ClickEvent.Action.RUN_COMMAND && click.getValue().startsWith(prefix)) {
				return Optional.of(Boolean.TRUE);
			}
			return Optional.empty();
		}, Style.EMPTY);
		return found.orElse(Boolean.FALSE);
	}

	public static String plain(net.minecraft.text.OrderedText text) {
		StringBuilder builder = new StringBuilder();
		text.accept((index, style, codePoint) -> {
			builder.appendCodePoint(codePoint);
			return true;
		});
		return builder.toString();
	}
}
