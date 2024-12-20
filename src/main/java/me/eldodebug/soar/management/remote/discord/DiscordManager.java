package me.eldodebug.soar.management.remote.discord;

import com.google.gson.JsonObject;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

public class DiscordManager {
    public DiscordManager() {Multithreading.runAsync(this::checkDiscordValues);}

    public void checkDiscordValues(){
        DiscordStats discordStats = Glide.getInstance().getDiscordStats();
        JsonObject jsonObject = HttpUtils.readJson("https://discord.com/api/v9/invites/42PXqKvwxq?with_counts=true", null);

        if(jsonObject != null) {
            discordStats.setMemberCount(JsonUtils.getIntProperty(jsonObject, "approximate_member_count", -1));
            discordStats.setMemberOnline(JsonUtils.getIntProperty(jsonObject, "approximate_presence_count", -1));
        }
    }
}

