package me.eldodebug.soar.management.cape;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * The custom-cape folder: import (validate + normalise + copy), list, remove.
 * Stored capes are always normalised 64:32 PNGs named "<name>.png"; previews live in
 * the cache dir keyed by content hash. No Minecraft classes: unit-testable.
 */
public final class CapeStore {

	public static final class Entry {
		public final String name;
		public final File file;
		public final File preview;
		public final CapeImage image;

		Entry(String name, File file, File preview, CapeImage image) {
			this.name = name;
			this.file = file;
			this.preview = preview;
			this.image = image;
		}
	}

	public interface Problem {
		void report(File file, String message);
	}

	private final File dir;
	private final File cacheDir;

	public CapeStore(File dir, File cacheDir) {
		this.dir = dir;
		this.cacheDir = cacheDir;
	}

	public File getDir() {
		return dir;
	}

	/** Validates every file in the folder; invalid ones are reported and skipped, never deleted. */
	public List<Entry> scan(Problem problem) {
		List<Entry> out = new ArrayList<Entry>();
		File[] files = dir.listFiles();
		if (files == null) {
			return out;
		}
		Arrays.sort(files);
		for (File f : files) {
			if (!f.isFile() || f.getName().startsWith(".")) {
				continue;
			}
			try {
				out.add(entry(f));
			} catch (CapeImage.Invalid e) {
				problem.report(f, e.getMessage());
			} catch (IOException e) {
				problem.report(f, "Unreadable: " + e.getMessage());
			}
		}
		return out;
	}

	private Entry entry(File f) throws CapeImage.Invalid, IOException {
		CapeImage img = CapeImage.load(f);
		File preview = new File(cacheDir, "preview-" + CapeImage.hash(f) + ".png");
		if (!preview.isFile()) {
			cacheDir.mkdirs();
			ImageIO.write(img.preview(200), "png", preview);
		}
		String name = f.getName();
		int dot = name.lastIndexOf('.');
		return new Entry(dot > 0 ? name.substring(0, dot) : name, f, preview, img);
	}

	/** Copies a user-picked image into the folder as a normalised PNG; never overwrites. */
	public Entry importFile(File src) throws CapeImage.Invalid, IOException {
		CapeImage img = CapeImage.load(src);
		if (img.isBlank()) {
			throw new CapeImage.Invalid("The cape area of " + src.getName() + " is fully transparent");
		}
		dir.mkdirs();
		String base = src.getName();
		int dot = base.lastIndexOf('.');
		base = CapeImage.cleanName(dot > 0 ? base.substring(0, dot) : base);
		File dest = new File(dir, base + ".png");
		for (int i = 2; dest.exists(); i++) {
			dest = new File(dir, base + " (" + i + ").png");
		}
		File tmp = new File(dir, "." + dest.getName() + ".tmp");
		ImageIO.write(img.texture, "png", tmp);
		if (!tmp.renameTo(dest)) {
			tmp.delete();
			throw new IOException("Could not write " + dest);
		}
		return entry(dest);
	}

	public boolean remove(Entry e) {
		if (e.preview != null) {
			e.preview.delete();
		}
		return e.file.delete() || !e.file.exists();
	}
}
