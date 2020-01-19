package lv.dium.riskserver;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import lv.dium.riskgame.GameManipulator;
import lv.dium.riskgame.GameState;
import lv.dium.riskgame.MpConvertor;

import java.net.InetAddress;
import java.util.ArrayList;

import static lv.dium.riskserver.MpHelpers.MP_USER_ATTRIBUTE_KEY;

/**
 * Handler implementation for the HexWar server.
 */
public class MpServerHandler extends SimpleChannelInboundHandler<String> {
    static final ChannelGroup authorizedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private LoginHandler loginHandler;

    public MpServerHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured - send a greeting
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                (GenericFutureListener<Future<Channel>>) future -> {
                    ctx.writeAndFlush(
                            "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure HexWar server!\n");
                    ctx.writeAndFlush(
                            "Your session is protected by " +
                                    ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                    " cipher suite.\n");
                    ctx.writeAndFlush(
                            "You must name yourself to play a game!\n");

                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        String t = msg.toLowerCase();
        System.out.println("<--: " + t);

        // filter commands
        if (t.substring(0, 1).equals("!")) {

            MpUser authorizedUser = null;
            ArrayList<MpCommand> commandsToWrite = new ArrayList<>();
            GameWrapper gw = null;

            String command = null;
            String payload = "";
            String longCommand = null;
            String shortPayload = "";

            if (t.length() > 1) {
                command = t.substring(1, 2);
                if (t.length() > 2) {
                    payload = t.substring(2);
                }
            }
            if (t.length() > 4) {
                longCommand = t.substring(1, 5);
                if (t.length() > 5) {
                    shortPayload = t.substring(5);
                }
            }

            // login command
            if (command != null && command.equals("l")) {
                try {
                    MpUser user = loginHandler.getUserFromPayload(payload);
                    if (user.isAuthorized()) {
                        // add channel to authorized channels
                        authorizedUser = user;
                        authorizedChannels.add(ctx.channel());
                        commandsToWrite = MpHelpers.processOnLoginActions(user, ctx);
                    } else {
                        // return login failed
                        commandsToWrite.add(new MpCommand("=lf", "self"));
                    }
                } catch (Exception e) {
                    System.out.println("l process failed: " + e);
                }
            }
            // Authorization required for commands below
            if (authorizedUser == null) {
                // try to retrieve user from channel attribute
                authorizedUser = ctx.channel().attr(MP_USER_ATTRIBUTE_KEY).get();
            }


            if (authorizedUser != null && authorizedUser.isAuthorized() && (command != null)) {
                if (command.equals("l")) {
                    // it's all ok already
                } else if (command.equals("g")) {
                    // greeting command
                    try {
                        //String playerNick = payload;
                        String playerId = authorizedUser.getUsername();

                        // Find/create game OR put player into queue
                        synchronized (this) {
                            try {
                                gw = Pool.gamesIndexedByPlayer.get(playerId);
                            } catch (Exception e) {
                                System.out.println("Pool.player.get failed: " + e);
                            }

                            if (gw == null) { // handle queue pool
                                MpHelpers.passUserToQueue(authorizedUser);

                                // Create a game if enough users
                                if (MpHelpers.queueFilled()) {
                                    gw = MpHelpers.createGame();

                                    // We either got a running game now or something went wrong
                                    try {
                                        if (gw.g != null) {
                                            commandsToWrite.add(new MpCommand("=gg" + gw.gameAsJson(), "all"));
                                        } else {
                                            commandsToWrite.add(new MpCommand("=gf", "self"));
                                        }
                                    } catch (Exception e) {
                                        System.out.println("g post-process failed: " + e);
                                    }

                                } else {
                                    // waiting to reach player limit
                                    commandsToWrite.add(new MpCommand("=gw", "self"));
                                }
                            } else { // got a game running
                                commandsToWrite.add(new MpCommand("=gg" + gw.gameAsJson(), "self"));
                                commandsToWrite.add(new MpCommand("=tr" + playerId, "others"));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("g command failed: " + e);
                    }
                } else {
                    // Any other command will be matched against game processors if game exists
                    // We will expect some kind of universal object so we can broadcast it
                    gw = Pool.gamesIndexedByPlayer.get(authorizedUser.getUsername());
                    if (gw.g != null) {
                        try {
                            Action resolution = GameManipulator.handleAction(
                                    gw.g, // game state
                                    gw.g.findColorByUsername(authorizedUser.getUsername()), // current user
                                    longCommand,
                                    shortPayload
                            );
                            commandsToWrite = MpConvertor.convertEffectsToCommands(resolution.getEffects());
                        } catch (Exception e) {
                            System.out.println("[WARN] Game resolution processing failed. " + e);
                        }
                    }
                }
            }

            // Broadcast commands if any
            if (commandsToWrite.size() > 0) {
                MpHelpers.broadcastCommands(
                        commandsToWrite, // commands
                        ctx, // current user's channel context
                        (gw != null) ? gw.broadcastList() : null // current game's broadcast list
                );
            }
        }

        // Close the connection if the client has sent 'exit'.
        if ("exit".equals(msg.toLowerCase())) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}