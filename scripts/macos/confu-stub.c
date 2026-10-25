/* Mach-O entry for Confu Client.app. Launch Services applies NSHighResolutionCapable
 * to this binary because it lives in Contents/MacOS. It then replaces itself with
 * the bundled Java 8 launcher. No -XstartOnFirstThread: that flag deadlocks Swing.
 * The C runtime is not linked; only libSystem calls are used.
 */
typedef unsigned int uint32_t;

int NSGetExecutablePath(char *buf, uint32_t *size);
int chdir(const char *path);
int execve(const char *path, char *const argv[], char *const envp[]);
void _exit(int status);

static char exe_buf[8192];
static char res_buf[8192];
static char java_buf[8192];
static char jar_buf[8192];
static char *args[160];

static int slen(const char *s) {
	int n = 0;
	while (s[n] != 0) {
		n++;
	}
	return n;
}

static void scopy(char *dst, const char *src, int n) {
	int i;
	for (i = 0; i < n; i++) {
		dst[i] = src[i];
	}
	dst[n] = 0;
}

static void sadd(char *dst, const char *src) {
	int n = slen(dst);
	int m = slen(src);
	scopy(dst + n, src, m);
}

static int match_at(const char *s, int i, const char *pfx) {
	int k = 0;
	while (pfx[k] != 0) {
		if (s[i + k] != pfx[k]) {
			return 0;
		}
		k++;
	}
	return 1;
}

int main(int argc, char **argv, char **envp) {
	uint32_t sz = sizeof exe_buf;
	int i;
	int last = -1;
	int n = 0;
	const char *mark = "/Contents/MacOS/";

	if (NSGetExecutablePath(exe_buf, &sz) != 0) {
		_exit(1);
	}
	for (i = 0; exe_buf[i] != 0; i++) {
		if (match_at(exe_buf, i, mark)) {
			last = i;
		}
	}
	if (last < 0) {
		_exit(2);
	}
	scopy(res_buf, exe_buf, last);
	sadd(res_buf, "/Contents/Resources");
	scopy(java_buf, res_buf, slen(res_buf));
	sadd(java_buf, "/jre/bin/java");
	scopy(jar_buf, res_buf, slen(res_buf));
	sadd(jar_buf, "/CONFUSION3H-launcher.jar");
	chdir(res_buf);

	args[n++] = java_buf;
	args[n++] = "-Dapple.awt.application.name=Confu Client";
	args[n++] = "-Xdock:name=Confu Client";
	args[n++] = "-Dsun.java2d.dpiaware=true";
	args[n++] = "-Dapple.awt.graphics.UseQuartz=true";
	args[n++] = "-Dawt.useSystemAAFontSettings=on";
	args[n++] = "-Dswing.aatext=true";
	args[n++] = "-jar";
	args[n++] = jar_buf;
	for (i = 1; i < argc && n < 158; i++) {
		args[n++] = argv[i];
	}
	args[n] = 0;
	execve(java_buf, args, envp);
	_exit(127);
	return 127;
}
