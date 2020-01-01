package lv.dium.riskserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.util.HashMap;
import java.util.Map;

/* need some way to make this thread safe */

public class Pool {
    public static Map<String, Game> gamesIndexedByPlayer = new HashMap<>();
    public static Map<String, ChannelHandlerContext> userChannels = new HashMap<String, io.netty.channel.ChannelHandlerContext>();

    public static synchronized ChannelHandlerContext putAndGetUserChannel(String username, ChannelHandlerContext newChannelContext) {
        ChannelHandlerContext oldContext = null;
        try {
            oldContext = userChannels.get(username);
        }
        catch (Exception e){
            System.out.println("Pool [userChannels.get] : " + e);
        }
        if(newChannelContext != null) {
            userChannels.put(username, newChannelContext);
        }

        return oldContext;
    }
}
