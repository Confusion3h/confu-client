package me.eldodebug.soar.management.command.impl;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.management.mods.impl.MusicPlayerMod;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.music.NowPlaying;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/** .soarcmd music connect|disconnect|play|pause|toggle|next|prev|reload|folder|status|vol N */
public class MusicCommand extends Command {

	public MusicCommand() {
		super("music");
	}

	@Override
	public void onCommand(String message) {
		MusicManager m = Glide.getInstance().getMusicManager();
		String[] a = message.trim().split("\\s+");
		String sub = a.length == 0 || a[0].equals("music") ? "status" : a[0].toLowerCase();
		MusicPlayerMod mod = MusicPlayerMod.getInstance();
		switch (sub) {
		case "connect":
			if (mod == null || !mod.isToggled()) {
				say("Enable the Music Player mod and set Source to Spotify first.");
				return;
			}
			m.connectSpotify(mod.getClientId());
			break;
		case "disconnect":
			m.disconnectSpotify();
			break;
		case "play":
			m.play(true);
			break;
		case "pause":
			m.play(false);
			break;
		case "toggle":
			m.togglePause();
			break;
		case "next":
			m.next();
			break;
		case "prev":
		case "previous":
			m.previous();
			break;
		case "reload":
			m.rescan();
			break;
		case "folder":
			MusicManagerHolder.open(m);
			break;
		case "vol":
		case "volume":
			try {
				int v = Integer.parseInt(a[1]);
				if (m.getSource() == MusicManager.Source.SPOTIFY) {
					m.setSpotifyVolume(v);
				} else {
					m.setLocalVolume(Math.max(0, Math.min(100, v)) / 100f);
				}
			} catch (Exception e) {
				say("Usage: .soarcmd music vol 0-100");
			}
			break;
		default:
			NowPlaying np = m.getNowPlaying();
			say("Source: " + m.getSource() + (m.getSource() == MusicManager.Source.SPOTIFY
					? (m.getSpotify().isAuthorized() ? " (connected)" : " (not connected)")
					: " (" + m.getMusicDir().getAbsolutePath() + ")"));
			say(np.isEmpty() ? "Nothing playing" : (np.playing ? "Playing: " : "Paused: ") + np.title + (np.artist != null ? " - " + np.artist : ""));
			say("Commands: connect, disconnect, play, pause, toggle, next, prev, vol N, reload, folder");
		}
	}

	private static void say(String s) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new ChatComponentText("\u00a77[Music] \u00a7f" + s));
		}
	}

	private static final class MusicManagerHolder {
		static void open(MusicManager m) {
			try {
				java.awt.Desktop.getDesktop().open(m.getMusicDir());
			} catch (Exception e) {
				say(m.getMusicDir().getAbsolutePath());
			}
		}
	}
}
