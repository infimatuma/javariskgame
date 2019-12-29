package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.HashMap;
import java.util.Map;

/* need some way to make this thread safe */

public class Pool {
    public static Map<Number, Game> games = new HashMap<>();
    public static Map<String, Number> players = new HashMap<>();
    public static Map<String, MpUser> channelUsers = new HashMap<>();
    public static Map<String, MpUser> users = new HashMap<>();
    public static Map<String, Channel> userChannels = new HashMap<>();
    public static Map<Number, ChannelGroup> gameChannelGroups = new HashMap<Number, ChannelGroup>();

    /*
    public static void putIntoGames(Number n, Game g){
        games.put(n, g);
    }
    public static Game getFromGames(Number n){
        return games.get(n);
    }

    public static void putIntoPlayers(String p, Number g){
        players.put(p, g);
    }
    public static Game getFromPlayers(String n){
        return games.get(n);
    }

    public static void putIntoChannelUsers(String p, MpUser u){
        channelUsers.put(p, u);
    }
    public static MpUser getFromChannelUsers(String n){
        return channelUsers.get(n);
    }

    public static void putIntoUserChannels(String p, Channel u){
        userChannels.put(p, u);
    }
    public static Channel getFromUserChannels(String n){
        return userChannels.get(n);
    }*/
}
