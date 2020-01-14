package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class MpHelpers {
    public static final LinkedBlockingQueue<MpUser> usersInQueue = new LinkedBlockingQueue<>();
    final static AttributeKey<MpUser> MP_USER_ATTRIBUTE_KEY = AttributeKey.valueOf("authorized_user");
    static final int maximumPlayersPerGame = 2;

    /** Send commands to clients
     *
     * @param mpCommands - commands to send
     * @param ctx - active client's ChannelHandlerContext
     * @param broadcastList - all clients DefaultChannelGroup
     */
    public static void broadcastCommands(ArrayList<MpCommand> mpCommands, ChannelHandlerContext ctx, DefaultChannelGroup broadcastList) {
        try {
            for (MpCommand nextCommand : mpCommands) {
                if (nextCommand.getValue() != null) {
                    if(nextCommand.getSendTo().equals("self")) {
                        try {
                            System.out.println("Point-sending to channel[" + ctx.channel().id() + "]: " + nextCommand);
                            ctx.channel().writeAndFlush(nextCommand.getValue() + '\n');
                        } catch (Exception e) {
                            System.out.println("[WARN] Channel point-sending failed (" + nextCommand + ") " + e);
                        }
                    }
                    else{
                        if(broadcastList != null) {
                            for (Channel c : broadcastList) {
                                try {
                                    if (!nextCommand.getSendTo().equals("others") || (c != ctx.channel())) {
                                        System.out.println("Broadcast to channel[" + c.id() + "]: " + nextCommand);
                                        c.writeAndFlush(nextCommand.getValue() + '\n');
                                    }
                                } catch (Exception e) {
                                    System.out.println("[WARN] Channel write failed during broadcast (" + nextCommand + ") " + e);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("Failed dispatching results. " + e);
        }
    }

    public static GameWrapper createGame() {
        GameWrapper gw = new GameWrapper();

        // create game's channel group
        DefaultChannelGroup gameChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        gw.setChannelGroup(gameChannelGroup);

        try {
            for (int i = 0; i < maximumPlayersPerGame; i++) {
                MpUser nextUser = usersInQueue.poll();
                if(nextUser != null) {
                    gw.g.addUser(nextUser.getUsername());

                    // add users most recent channel to game's channel group
                    gameChannelGroup.add(Pool.putAndGetUserChannel(nextUser.getUsername(), null).channel());

                    try {
                        Pool.gamesIndexedByPlayer.put(nextUser.getUsername(), gw);
                        System.out.println("Game index updated for player " + nextUser.getUsername());
                    } catch (Exception e) {
                        System.out.println("gamesIndexedByPlayer failed: " + e);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Game add users failed: " + e);
        }

        try {
            GameManipulator.start(gw.g, "basic");
        }
        catch (Exception e){
            System.out.println("Game start failed: " + e);
        }

        return gw;
    }

    public static void passUserToQueue(MpUser authorizedUser) {
        if(!usersInQueue.contains(authorizedUser)) {
            usersInQueue.add(authorizedUser);
            System.out.println("Pool.add: " + authorizedUser.getUsername());
        }
    }

    public static boolean queueFilled() {
        return usersInQueue.size() >= maximumPlayersPerGame;
    }

    public static ArrayList<MpCommand> processOnLoginActions(MpUser user, ChannelHandlerContext ctx) {
        ArrayList<MpCommand> commands = new ArrayList<>();

        ctx.channel().attr(MP_USER_ATTRIBUTE_KEY).set(user);
        ChannelHandlerContext oldCtx = Pool.putAndGetUserChannel(user.getUsername(), ctx);

        if (oldCtx == null) {
            System.out.println("New user login");
        } else {
            System.out.println("User re-login");
        }

        // If ongoing game exists
        GameWrapper existingGameWrapper = Pool.gamesIndexedByPlayer.get(user.getUsername());

        // If old channel for the same user exists - close it
        if (oldCtx != null) {
            System.out.println("Will match connections");
            // only close it if this is a different channel
            if (oldCtx.channel().id() != ctx.channel().id()) {
                System.out.println("Will close old connection");
                if (existingGameWrapper != null) {
                    System.out.println("We have a GameWrapper - so will call replaceOrSetChannel");
                    existingGameWrapper.replaceOrSetChannel(oldCtx.channel(), ctx.channel());
                }
                System.out.println("Writing [fc] to old channel and closing");
                oldCtx.channel().writeAndFlush("=fcNewChannel" + '\n');
                oldCtx.channel().close();
            } else {
                System.out.println("This is the same connection: " + oldCtx.channel().id() + " == " + ctx.channel().id());
            }
        }

        if (existingGameWrapper != null) {
            // return login succeed, ongoing game exists
            commands.add(new MpCommand("=lg", "self"));
        } else {
            // return login succeed
            commands.add(new MpCommand("=ls", "self"));
        }

        return commands;
    }
}
