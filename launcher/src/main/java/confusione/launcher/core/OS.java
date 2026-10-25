package confusione.launcher.core;

import java.util.Map;

/** Mojang rule/native naming for the current platform. */
public final class OS {

	public static final String NAME;
	public static final String ARCH_BITS;
	/** aarch64 / arm64. Mojang's 1.8.9 natives are x86_64; ARM builds overlay their own. */
	public static final boolean ARM;

	static {
		String n = System.getProperty("os.name").toLowerCase();
		NAME = n.contains("win") ? "windows" : n.contains("mac") ? "osx" : "linux";
		String a = System.getProperty("os.arch", "").toLowerCase();
		ARM = a.equals("aarch64") || a.equals("arm64");
		ARCH_BITS = a.contains("64") ? "64" : "32";
	}

	private OS() {}

	/** Evaluates a Mojang "rules" array; no rules means allowed. */
	public static boolean allowed(Object rules) {
		if (rules == null) {
			return true;
		}
		boolean allow = false;
		for (Object o : Json.arr(rules)) {
			Map<String, Object> r = Json.obj(o);
			Map<String, Object> os = r.containsKey("os") ? Json.obj(r.get("os")) : null;
			if (os == null || NAME.equals(Json.str(os, "name"))) {
				allow = "allow".equals(Json.str(r, "action"));
			}
		}
		return allow;
	}
}
