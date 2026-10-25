import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Builds launch-compat.jar:
 *   Launch.&lt;init&gt; reads java.class.path on Java 9+ and excludes com.sun. / jdk.
 *   MixinServiceLaunchWrapper.getClassBytes falls back to Java 8 rt.jar bytes,
 *   because Mixin 0.7's ASM cannot parse newer JDK class files.
 *
 * Usage: java BuildCompat launchwrapper-1.12.jar client.jar classesDir out.jar
 */
public class BuildCompat implements Opcodes {
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.err.println("Usage: BuildCompat launchwrapper.jar client.jar classesDir out.jar");
			System.exit(2);
		}
		byte[] launch = patchLaunch(entry(args[0], "net/minecraft/launchwrapper/Launch.class"));
		byte[] mixin = patchMixin(entry(args[1], "org/spongepowered/asm/service/mojang/MixinServiceLaunchWrapper.class"));
		byte[] urls = Files.readAllBytes(Paths.get(args[2], "confusione/launchcompat/ClasspathUrls.class"));
		byte[] jdk = Files.readAllBytes(Paths.get(args[2], "confusione/launchcompat/JdkClassBytes.class"));
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(args[3]));
		try {
			put(jos, "net/minecraft/launchwrapper/Launch.class", launch);
			put(jos, "org/spongepowered/asm/service/mojang/MixinServiceLaunchWrapper.class", mixin);
			put(jos, "confusione/launchcompat/ClasspathUrls.class", urls);
			put(jos, "confusione/launchcompat/JdkClassBytes.class", jdk);
		} finally {
			jos.close();
		}
		System.out.println("Wrote " + args[3]);
	}

	private static byte[] entry(String jar, String name) throws Exception {
		JarFile jf = new JarFile(jar);
		try {
			JarEntry e = jf.getJarEntry(name);
			if (e == null) {
				throw new IllegalStateException(name + " missing from " + jar);
			}
			InputStream in = jf.getInputStream(e);
			try {
				byte[] buf = new byte[8192];
				java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
				int n;
				while ((n = in.read(buf)) != -1) {
					out.write(buf, 0, n);
				}
				return out.toByteArray();
			} finally {
				in.close();
			}
		} finally {
			jf.close();
		}
	}

	private static void put(JarOutputStream jos, String name, byte[] data) throws Exception {
		jos.putNextEntry(new JarEntry(name));
		jos.write(data);
		jos.closeEntry();
	}

	private static byte[] patchLaunch(byte[] in) {
		ClassReader cr = new ClassReader(in);
		ClassWriter cw = new ClassWriter(0);
		cr.accept(new ClassVisitor(ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
				if ("<init>".equals(name) && "()V".equals(desc)) {
					MethodVisitor mv = super.visitMethod(access, name, desc, sig, exc);
					mv.visitCode();
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
					mv.visitTypeInsn(NEW, "net/minecraft/launchwrapper/LaunchClassLoader");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESTATIC, "confusione/launchcompat/ClasspathUrls", "urls", "()[Ljava/net/URL;", false);
					mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/launchwrapper/LaunchClassLoader", "<init>", "([Ljava/net/URL;)V", false);
					mv.visitFieldInsn(PUTSTATIC, "net/minecraft/launchwrapper/Launch", "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;");
					exclude(mv, "com.sun.");
					exclude(mv, "jdk.");
					mv.visitTypeInsn(NEW, "java/util/HashMap");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
					mv.visitFieldInsn(PUTSTATIC, "net/minecraft/launchwrapper/Launch", "blackboard", "Ljava/util/Map;");
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
					mv.visitFieldInsn(GETSTATIC, "net/minecraft/launchwrapper/Launch", "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "setContextClassLoader", "(Ljava/lang/ClassLoader;)V", false);
					mv.visitInsn(RETURN);
					mv.visitMaxs(3, 1);
					mv.visitEnd();
					return null;
				}
				return super.visitMethod(access, name, desc, sig, exc);
			}
		}, 0);
		return cw.toByteArray();
	}

	private static void exclude(MethodVisitor mv, String prefix) {
		mv.visitFieldInsn(GETSTATIC, "net/minecraft/launchwrapper/Launch", "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;");
		mv.visitLdcInsn(prefix);
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/launchwrapper/LaunchClassLoader", "addClassLoaderExclusion", "(Ljava/lang/String;)V", false);
	}

	private static byte[] patchMixin(byte[] in) {
		ClassReader cr = new ClassReader(in);
		ClassWriter cw = new ClassWriter(0);
		cr.accept(new ClassVisitor(ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
				if ("getClassBytes".equals(name) && "(Ljava/lang/String;Ljava/lang/String;)[B".equals(desc)) {
					MethodVisitor mv = super.visitMethod(access, name, desc, sig, exc);
					mv.visitCode();
					mv.visitFieldInsn(GETSTATIC, "net/minecraft/launchwrapper/Launch", "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;");
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/launchwrapper/LaunchClassLoader", "getClassBytes", "(Ljava/lang/String;)[B", false);
					mv.visitVarInsn(ASTORE, 3);
					mv.visitVarInsn(ALOAD, 3);
					Label fallback = new Label();
					mv.visitJumpInsn(IFNULL, fallback);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ARETURN);
					mv.visitLabel(fallback);
					mv.visitFrame(F_FULL, 4, new Object[] {
						"org/spongepowered/asm/service/mojang/MixinServiceLaunchWrapper",
						"java/lang/String",
						"java/lang/String",
						"[B"
					}, 0, new Object[0]);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, "confusione/launchcompat/JdkClassBytes", "read", "(Ljava/lang/String;)[B", false);
					mv.visitInsn(ARETURN);
					mv.visitMaxs(2, 4);
					mv.visitEnd();
					return null;
				}
				return super.visitMethod(access, name, desc, sig, exc);
			}
		}, 0);
		return cw.toByteArray();
	}
}
