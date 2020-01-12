package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class MpHelpers {
    public static final LinkedBlockingQueue<MpUser> usersInQueue = new LinkedBlockingQueue<>();
    static final int maximumPlayersPerGame = 2;

    /**
     * Converts GameEffect-s into MpCommand-s.
     * Null-safe.
     *
     * @param effects - effects to convert
     * @return
     */
    public static ArrayList<MpCommand> convertEffectsToCommands(ArrayList<GameEffect> effects) {
        ArrayList<MpCommand> commands = new ArrayList<MpCommand>();

        String lastCommandValue = null;
        String currentString = "";

        try {
            if (effects != null && effects.size() > 0) {
                for (GameEffect gameEffect : effects) {
                    String nextCommand = gameEffect.getCommand();
                    String nextValue = gameEffect.getValues();

                    System.out.println("Command [" + nextCommand + "] with body [" + nextValue + "]");

                    try {
                        if (nextCommand != null) {
                            if (nextCommand != lastCommandValue) {
                                if (lastCommandValue != null) {
                                    commands.add(new MpCommand(currentString, "all"));
                                }

                                currentString = nextCommand;
                                lastCommandValue = nextCommand;
                            }
                        }
                        currentString = currentString + nextValue;
                    } catch (Exception e) {
                        System.out.println("Command formatting in-loop error. " + e);
                    }
                }
                try {
                    if (currentString.length() > 0) {
                        commands.add(new MpCommand(currentString, "all"));
                    }
                } catch (Exception e) {
                    System.out.println("Command formatting later error. " + e);
                }
            } else {
                commands.add(new MpCommand("=err", "self"));
            }
        } catch (Exception e) {
            System.out.println("Failed formatting commands. " + e);
        }

        return commands;
    }

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
                gw.g.addUser(nextUser);

                // add users most recent channel to game's channel group
                gameChannelGroup.add(Pool.putAndGetUserChannel(nextUser.getUsername(), null).channel());

                try{
                    Pool.gamesIndexedByPlayer.put(nextUser.getUsername(), gw);
                    System.out.println("Game index updated for player " + nextUser.getUsername());
                }
                catch (Exception e){
                    System.out.println("gamesIndexedByPlayer failed: " + e);
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
}
