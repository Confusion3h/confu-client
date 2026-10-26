package me.eldodebug.soar.logger;

import me.eldodebug.confu.ConfuClient;

public final class GlideLogger {

	private GlideLogger() {
	}

	public static void info(String message) {
		ConfuClient.LOGGER.info(message);
	}

	public static void warn(String message) {
		ConfuClient.LOGGER.warn(message);
	}

	public static void error(String message) {
		ConfuClient.LOGGER.error(message);
	}

	public static void error(String message, Throwable t) {
		ConfuClient.LOGGER.error(message, t);
	}
}
