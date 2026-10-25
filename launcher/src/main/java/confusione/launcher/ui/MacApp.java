package confusione.launcher.ui;

import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.swing.SwingUtilities;

import confusione.launcher.core.MacFocus;

/**
 * macOS Dock integration through the JDK 8 com.apple.eawt API, looked up by
 * reflection so other platforms never load it. Sets the Confu artwork as the Dock
 * icon and brings the launcher back when its Dock icon is clicked while it is hidden.
 */
public final class MacApp {

	private MacApp() {}

	public static void install(final LauncherFrame frame) {
		if (!MacFocus.isMac()) {
			return;
		}
		try {
			Class<?> appClass = Class.forName("com.apple.eawt.Application");
			Object app = appClass.getMethod("getApplication").invoke(null);
			Image icon = Icons.dockImage();
			if (icon != null) {
				appClass.getMethod("setDockIconImage", Image.class).invoke(app, icon);
			}
			Class<?> listener = Class.forName("com.apple.eawt.AppReOpenedListener");
			Class<?> eventListener = Class.forName("com.apple.eawt.AppEventListener");
			Object proxy = Proxy.newProxyInstance(MacApp.class.getClassLoader(), new Class<?>[] { listener }, new InvocationHandler() {
				public Object invoke(Object self, Method m, Object[] args) {
					if (m.getName().equals("appReOpened")) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								frame.restoreFromDock();
							}
						});
						return null;
					}
					if (m.getName().equals("hashCode")) {
						return System.identityHashCode(self);
					}
					if (m.getName().equals("equals")) {
						return self == args[0];
					}
					if (m.getName().equals("toString")) {
						return "AppReOpenedListener";
					}
					return null;
				}
			});
			appClass.getMethod("addAppEventListener", eventListener).invoke(app, proxy);
		} catch (Throwable ignored) {
		}
	}
}
