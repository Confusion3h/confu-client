package confusione.launchcompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Mixin 0.7 ships ASM 5, which stops at class-file version 52 (Java 8).
 * When it inspects java.lang.Thread it must see Java 8 bytes, not the
 * running JDK. Point -Dconfu.java8.rt at a Java 8 rt.jar.
 */
public final class JdkClassBytes {
	private static volatile JarFile rt;

	private JdkClassBytes() {}

	public static byte[] read(String binaryName) throws IOException {
		if (binaryName == null) {
			return null;
		}
		String path = binaryName.replace('.', '/') + ".class";
		byte[] fromRt = readRt(path);
		if (fromRt != null) {
			return fromRt;
		}
		InputStream in = ClassLoader.getSystemResourceAsStream(path);
		if (in == null) {
			return null;
		}
		try {
			return readAll(in);
		} finally {
			in.close();
		}
	}

	private static byte[] readRt(String path) throws IOException {
		String loc = System.getProperty("confu.java8.rt");
		if (loc == null || loc.isEmpty()) {
			return null;
		}
		JarFile jar = rt;
		if (jar == null) {
			synchronized (JdkClassBytes.class) {
				jar = rt;
				if (jar == null) {
					jar = new JarFile(loc);
					rt = jar;
				}
			}
		}
		JarEntry e = jar.getJarEntry(path);
		if (e == null) {
			return null;
		}
		InputStream in = jar.getInputStream(e);
		try {
			return readAll(in);
		} finally {
			in.close();
		}
	}

	private static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = in.read(buf)) != -1) {
			out.write(buf, 0, n);
		}
		return out.toByteArray();
	}
}
