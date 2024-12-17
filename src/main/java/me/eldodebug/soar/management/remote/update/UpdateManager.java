package me.eldodebug.soar.management.remote.update;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.network.HttpUtils;

import java.util.Iterator;

public class UpdateManager {
    public UpdateManager() {Multithreading.runAsync(this::checkUpdates);}

    public void checkUpdates(){
        Update update = Glide.getInstance().getUpdateInstance();
        JsonObject jsonObject = HttpUtils.readJson("https://glideclient.github.io/data/updates/release.json", null);
        if(jsonObject != null) {

            JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "updates");

            if(jsonArray != null) {

                Iterator<JsonElement> iterator = jsonArray.iterator();

                while(iterator.hasNext()) {

                    JsonElement jsonElement = iterator.next();
                    Gson gson = new Gson();
                    JsonObject updateJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                    update.setUpdateLink(JsonUtils.getStringProperty(updateJsonObject, "updatelink", "https://glideclient.github.io/"));
                    update.setVersionString(JsonUtils.getStringProperty(updateJsonObject, "versionstring", "something is broken lmao"));
                    update.setBuildID(JsonUtils.getIntProperty(updateJsonObject, "version", 0));
                    update.checkForUpdates();
                }
            }
        }
    }
}
