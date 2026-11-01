package me.eldodebug.confu.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.net.URL;

/** Plays a raw asset file, like the 1.8.9 Soar Sound helper. */
public final class Sound {

	private Sound() {
	}

	public static void play(String location, boolean uiSound) {
		URL resource = Sound.class.getClassLoader().getResource("assets/minecraft/" + location);
		if (resource == null) {
			return;
		}
		try {
			Clip clip = AudioSystem.getClip();
			AudioInputStream stream = AudioSystem.getAudioInputStream(resource);
			clip.open(stream);
			clip.start();
			clip.addLineListener(event -> {
				if (event.getType() == LineEvent.Type.STOP) {
					clip.close();
					try {
						stream.close();
					} catch (Exception ignored) {
					}
				}
			});
		} catch (Exception ignored) {
		}
	}
}
