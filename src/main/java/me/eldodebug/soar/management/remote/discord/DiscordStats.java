package me.eldodebug.soar.management.remote.discord;

public class DiscordStats {
    int membersCount = -1;
    int membersOnline = -1;

    public void setMemberCount(int in){
        this.membersCount = in;
    }
    public int getMemberCount(){return membersCount;}
    public void setMemberOnline(int in){
        this.membersOnline = in;
    }
    public int getMemberOnline(){return membersOnline;}

}
