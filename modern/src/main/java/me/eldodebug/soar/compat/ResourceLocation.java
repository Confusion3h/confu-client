package me.eldodebug.soar.compat;

/** Path under assets/minecraft/ in this jar, as 1.8.9 ResourceLocation("soar/...") resolved it. */
public final class ResourceLocation {

	private final String path;

	public ResourceLocation(String path) {
		this.path = path;
	}

	public String getResourcePath() {
		return path;
	}

	public String classpath() {
		return "/assets/minecraft/" + path;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ResourceLocation && ((ResourceLocation) o).path.equals(path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}
}
