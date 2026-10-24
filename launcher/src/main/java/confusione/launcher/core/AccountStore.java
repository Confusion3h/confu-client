package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Microsoft refresh token, profile name and UUID. Lives in the user data dir
 * ({@code ~/.CONFUSION3H/msa.properties} or {@code %APPDATA%\.CONFUSION3H}), never in the repo.
 * The Minecraft access token is not stored; it is minted again on launch.
 */
public final class AccountStore {

	public static final File FILE = new File(Settings.HOME, "msa.properties");

	public String refreshToken = "";
	public String name = "";
	public String uuid = "";

	public static AccountStore load() {
		AccountStore s = new AccountStore();
		Properties p = new Properties();
		if (FILE.isFile()) {
			try {
				InputStream in = new FileInputStream(FILE);
				try {
					p.load(in);
				} finally {
					in.close();
				}
			} catch (IOException ignored) {
			}
		}
		s.refreshToken = p.getProperty("refreshToken", "");
		s.name = p.getProperty("name", "");
		s.uuid = p.getProperty("uuid", "");
		return s;
	}

	public boolean signedIn() {
		return refreshToken != null && !refreshToken.isEmpty();
	}

	public void save() throws IOException {
		Properties p = new Properties();
		p.setProperty("refreshToken", refreshToken == null ? "" : refreshToken);
		p.setProperty("name", name == null ? "" : name);
		p.setProperty("uuid", uuid == null ? "" : uuid);
		Settings.HOME.mkdirs();
		OutputStream out = new FileOutputStream(FILE);
		try {
			p.store(out, "CONFUSION3H Microsoft session (private; delete to sign out)");
		} finally {
			out.close();
		}
		try {
			Files.setPosixFilePermissions(FILE.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
		} catch (UnsupportedOperationException ignored) {
			// Windows has no POSIX modes; the file still stays outside the repo
		}
	}

	public void clear() {
		refreshToken = "";
		name = "";
		uuid = "";
		FILE.delete();
	}
}
