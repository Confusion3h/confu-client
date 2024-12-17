package me.eldodebug.soar.management.remote.update;

import me.eldodebug.soar.Glide;

public class Update {
    String updateLink = "https://glideclient.github.io/";
    String updateVersionString = "something is broken lmao";
    int updateBuildID = 0;

    public void setUpdateLink(String in){
        this.updateLink = in;
    }
    public void setVersionString(String in){
        this.updateVersionString = in;
    }
    public void setBuildID(int in){
        this.updateBuildID = in;
    }
    public String getUpdateLink(){
        return updateLink;
    }
    public String getVersionString(){
        return updateVersionString;
    }
    public int getBuildID(){
        return updateBuildID;
    }


    public void checkForUpdates(){
        Glide g = Glide.getInstance();
        if (g.getVersionIdentifier() < this.updateBuildID){
            g.setUpdateNeeded(true);
        }
    }

}
