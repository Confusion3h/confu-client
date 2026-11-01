package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 AsyncScreenshotMod: off-thread PNG write when capturing. */
public final class AsyncScreenshotModule extends Module {

	private final BooleanSetting message = bool("message", "tt:MESSAGE", true);
	private final BooleanSetting clipboard = bool("clipboard", "tt:CLIPBOARD", false);

	public AsyncScreenshotModule() {
		super("async_screenshot", Category.OTHER, "tt:ASYNC_SCREENSHOT", "tt:ASYNC_SCREENSHOT_DESCRIPTION");
	}

	public boolean showMessage() {
		return isEnabled() && message.enabled();
	}

	public boolean toClipboard() {
		return isEnabled() && clipboard.enabled();
	}
}
