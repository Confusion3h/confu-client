package me.eldodebug.soar.utils;

import net.minecraft.util.Util;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.InputStream;

public class MacOSUtils {

    /**
     * LWJGL 2.9.2 on macOS creates the AWT frame, then sometimes leaves it
     * off-screen or behind the launcher. Pull every frame onto the visible
     * desktop and order it front. Safe to call once the display exists.
     */
    public static void orderGameWindowFront() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            return;
        }
        final Runnable nudge = new Runnable() {
            public void run() {
                try {
                    Frame[] frames = Frame.getFrames();
                    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                    for (int i = 0; i < frames.length; i++) {
                        Frame frame = frames[i];
                        int w = frame.getWidth();
                        int h = frame.getHeight();
                        if (w < 80 || h < 80) {
                            w = 854;
                            h = 480;
                            frame.setSize(w, h);
                        }
                        Point loc = frame.getLocation();
                        boolean off = loc.x < screen.x - 20 || loc.y < screen.y - 20
                                || loc.x > screen.x + screen.width - 40
                                || loc.y > screen.y + screen.height - 40;
                        if (off) {
                            frame.setLocation(
                                    screen.x + Math.max(0, (screen.width - w) / 2),
                                    screen.y + Math.max(0, (screen.height - h) / 2));
                        }
                        frame.setVisible(true);
                        frame.toFront();
                        frame.requestFocus();
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_ACTIVATED));
                    }
                    Class<?> appClass = Class.forName("com.apple.eawt.Application");
                    Object app = appClass.getMethod("getApplication").invoke(null);
                    try {
                        appClass.getMethod("requestForeground", boolean.class).invoke(app, Boolean.TRUE);
                    } catch (NoSuchMethodException ignored) {
                    }
                } catch (Throwable ignored) {
                }
            }
        };
        try {
            if (EventQueue.isDispatchThread()) {
                nudge.run();
            } else {
                EventQueue.invokeLater(nudge);
            }
        } catch (Throwable ignored) {
        }
        Thread later = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(700L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    EventQueue.invokeLater(nudge);
                } catch (Throwable ignored) {
                }
            }
        }, "confu-mac-window");
        later.setDaemon(true);
        later.start();
    }

    public static void setDockIcon(String path){
        if(Util.getOSType() != Util.EnumOS.OSX) return;  // Redundant check
        InputStream icon = MacOSUtils.class.getResourceAsStream(path); // you could probably use resource location but users can then change it with a TexturePack
        if (icon != null) {
            try {
                Class<?> appClass = Class.forName("com.apple.eawt.Application");
                appClass.getMethod("setDockIconImage", Image.class).invoke(appClass.getMethod("getApplication").invoke(null), ImageIO.read(icon));
            } catch (Exception e) { System.err.println("[ MacOS Utils ] Error setting dock icon: " + e.getMessage()); }
        } else { System.err.println("[ MacOS Utils ] Icon file could not be found"); }
    }

}