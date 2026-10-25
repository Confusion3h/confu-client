package confusione.launchcompat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/** Application classpath as URLs. Java 9+ has no URLClassLoader app loader. */
public final class ClasspathUrls {
	private ClasspathUrls() {}

	public static URL[] urls() {
		ClassLoader app = ClasspathUrls.class.getClassLoader();
		if (app instanceof URLClassLoader) {
			return ((URLClassLoader) app).getURLs();
		}
		String cp = System.getProperty("java.class.path", "");
		if (cp.isEmpty()) {
			return new URL[0];
		}
		String[] parts = cp.split(File.pathSeparator);
		URL[] urls = new URL[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				urls[i] = new File(parts[i]).toURI().toURL();
			} catch (Exception e) {
				throw new RuntimeException("Bad classpath entry: " + parts[i], e);
			}
		}
		return urls;
	}
}
