package confusione.launcher.core;

import java.io.File;

/** Files shipped next to the launcher jar for Java 9+ and for ARM natives. */
public final class LaunchCompat {

	private LaunchCompat() {}

	public static File installDir() {
		try {
			File self = new File(LaunchCompat.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			File dir = self.getParentFile();
			return dir == null ? new File(".") : dir;
		} catch (Exception e) {
			return new File(".");
		}
	}

	public static File compatJar() {
		File f = new File(installDir(), "launch-compat.jar");
		return f.isFile() ? f : null;
	}

	/** Java 8 rt.jar. Mixin 0.7 reads these bytes instead of the running JDK. */
	public static File java8Rt() {
		File f = new File(installDir(), "java8-rt.jar");
		return f.isFile() ? f : null;
	}

	/** Bundled LWJGL2 natives for this OS on ARM, or null. */
	public static File armNatives() {
		if (!OS.ARM) {
			return null;
		}
		String os = OS.NAME;
		File f = new File(installDir(), "natives" + File.separator + os + "-arm64");
		return f.isDirectory() ? f : null;
	}
}
