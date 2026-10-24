package confusione.launcher.core;

/** Callbacks from background work. Implementations must be thread-safe. */
public interface Progress {

	void stage(String text);

	/** 0..1, or negative for indeterminate. */
	void progress(double value);

	void log(String line);

	Progress NONE = new Progress() {
		public void stage(String text) {}
		public void progress(double value) {}
		public void log(String line) {}
	};

	/** Prints to stdout; used by the CLI modes. */
	Progress CONSOLE = new Progress() {
		private int lastPct = -1;
		public void stage(String text) { System.out.println("== " + text); }
		public synchronized void progress(double value) {
			int pct = (int) (value * 100);
			if (value >= 0 && pct / 10 != lastPct / 10) {
				lastPct = pct;
				System.out.println("   " + pct + "%");
			}
		}
		public void log(String line) { System.out.println(line); }
	};
}
