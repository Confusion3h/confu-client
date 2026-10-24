package confusione.launcher.core;

import java.util.Map;

/** Mojang rule/native naming for the current platform. */
public final class OS {

	public static final String NAME;
	public static final String ARCH_BITS = System.getProperty("os.arch").contains("64") ? "64" : "32";

	static {
		String n = System.getProperty("os.name").toLowerCase();
		NAME = n.contains("win") ? "windows" : n.contains("mac") ? "osx" : "linux";
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
