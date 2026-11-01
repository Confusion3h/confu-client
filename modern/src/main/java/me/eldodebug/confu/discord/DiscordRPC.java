package me.eldodebug.confu.discord;

import java.time.OffsetDateTime;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.discord.ipc.IPCClient;
import me.eldodebug.confu.discord.ipc.IPCListener;
import me.eldodebug.confu.discord.ipc.entities.RichPresence;
import me.eldodebug.confu.discord.ipc.exceptions.NoDiscordClientException;

/** 1.8.9 DiscordRPC: local Discord IPC rich presence under the user's application id. */
public class DiscordRPC {

	private IPCClient client;

	public void start(long appId) {
		client = new IPCClient(appId);
		client.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				RichPresence.Builder builder = new RichPresence.Builder();
				builder.setState("Playing Confu Client v" + ConfuClient.get().version())
						.setStartTimestamp(OffsetDateTime.now())
						.setLargeImage("icon");
				client.sendRichPresence(builder.build());
			}
		});
		try {
			client.connect();
		} catch (NoDiscordClientException e) {
			ConfuClient.LOGGER.warn("Discord client not running", e);
		}
	}

	public void stop() {
		if (client != null) {
			client.close();
		}
	}

	public boolean isStarted() {
		return client != null;
	}
}
