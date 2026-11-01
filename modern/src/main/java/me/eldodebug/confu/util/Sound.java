package me.eldodebug.confu.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/** Plays a raw asset file, like the 1.8.9 Soar Sound helper. */
public final class Sound {

	private Clip clip;

	public void loadClip(InputStream stream) {
		try {
			byte[] bytes = readAll(stream);
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes)));
		} catch (Exception ignored) {
		}
	}

	public void play() {
		if (clip != null) {
			clip.stop();
			clip.setFramePosition(0);
			clip.start();
		}
	}

	public void setVolume(float volume) {
		if (clip == null || volume <= 0F) {
			return;
		}
		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		gain.setValue((float) (Math.log(volume) / Math.log(10.0) * 20.0));
	}

	private static byte[] readAll(InputStream in) throws Exception {
		try (in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toByteArray();
		}
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
