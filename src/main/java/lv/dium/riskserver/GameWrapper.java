package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;

public class GameWrapper {
    private DefaultChannelGroup channelGroup;
    public GameState g;

    public GameWrapper(GameState g){
        this.g = g;
    }

    public GameWrapper(){
        this.g = new GameState();
    }

    public void setChannelGroup(DefaultChannelGroup gameChannelGroup) {
        this.channelGroup = gameChannelGroup;
    }

    public DefaultChannelGroup broadcastList() {
        return this.channelGroup;
    }

    public void replaceOrSetChannel(Channel oldChannel, Channel newChannel) {
        System.out.println("Need to replace channel " + oldChannel.id() + " with " + newChannel.id());

        if (oldChannel == null) return;
        if (newChannel == null) return;

        g.lock.lock();
        System.out.println("Got game lock");

        try {
            for (Channel c : broadcastList()) {
                if (c.id() == oldChannel.id()) {
                    broadcastList().remove(c);
                    System.out.println("Removed one channel");
                }
            }
            broadcastList().add(newChannel);
            System.out.println("Added one channel");
        } catch (Exception e) {
            System.out.println("Failed replaceOrSetChannel " + e);
        } finally {
            g.lock.unlock();
            System.out.println("Released game lock");
        }
    }
}
