package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handler implementation for the HexWar server.
 */
public class MpServerHandler extends SimpleChannelInboundHandler<String> {
    static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final ChannelGroup authorizedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private LoginHandler loginHandler;

    final static AttributeKey<MpUser> MP_USER_ATTRIBUTE_KEY = AttributeKey.valueOf("authorized_user");

    public MpServerHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        ctx.writeAndFlush(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure HexWar server!\n");
                        ctx.writeAndFlush(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.\n");
                        ctx.writeAndFlush(
                                "You must name yourself to play a game!\n");

                    }
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String t = msg.toLowerCase();

        System.out.println("["+t.substring(0,1)+"] ["+t.substring(1,2)+"]");

        allChannels.add(ctx.channel());

        // filter commands
        if(t.substring(0,1).equals("!")){

            MpUser authorizedUser = null;

            // login command
            if(t.substring(1,2).equals("l")){
                try {
                    // payload should be a json string, but we do not test it here
                    String payload = t.substring(2, t.length());

                    MpUser user = loginHandler.getUserFromPayload(payload);
                    if (user.isAuthorized()) {
                        // add channel to authorized channels
                        authorizedUser = user;
                        authorizedChannels.add(ctx.channel());

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
                                    System.out.println("We have a game - so will call replaceOrSetChannel");
                                    existingGameWrapper.replaceOrSetChannel(oldCtx.channel(), ctx.channel());
                                }
                                System.out.println("Writingf [fc] to old channel and closing");
                                oldCtx.channel().writeAndFlush("=fcNewChannel" + '\n');
                                oldCtx.channel().close();
                            } else {
                                System.out.println("This is the same connection: " + oldCtx.channel().id() + " == " + ctx.channel().id());
                            }
                        }

                        if (existingGameWrapper != null) {
                            // return login succeed, ongoing game exists
                            ctx.channel().writeAndFlush("=lg" + '\n');
                        } else {
                            // return login succeed
                            ctx.channel().writeAndFlush("=ls" + '\n');
                        }
                    } else {
                        // return login failed
                        ctx.channel().writeAndFlush("=lf" + '\n');
                    }
                }
                catch (Exception e){
                    System.out.println("l process failed: " + e);
                }
            }
            // Authorization required for commands below
            if(authorizedUser == null) {
                // try to retrieve user from channel attribute
                authorizedUser = ctx.channel().attr(MP_USER_ATTRIBUTE_KEY).get();
            }

            String command = t.substring(1, 2);
            if(authorizedUser != null && authorizedUser.isAuthorized() && command != null) {
                // greeting command
                // Not in-game yet
                if (command.equals("l")) {
                    // it's all ok already
                }
                else if (command.equals("g")) {
                    try {
                        String playerNick = t.substring(2, t.length());
                        String playerId = authorizedUser.getUsername();
                        GameWrapper gw = null;
                        ArrayList<MpCommand> commands = new ArrayList<>();

                        // Find/create game OR put player into queue
                        synchronized (this) {
                            try {
                                gw = Pool.gamesIndexedByPlayer.get(playerId);
                            }
                            catch (Exception e){
                                System.out.println("Pool.player.get failed: " + e);
                            }

                            if (gw == null) { // handle queue pool
                                MpHelpers.passUserToQueue(authorizedUser);

                                // Create a game if enough users
                                if (MpHelpers.queueFilled()) {
                                    gw = MpHelpers.createGame();

                                    // We either got a running game now or something went wrong
                                    try {
                                        if (gw != null && gw.g != null) {
                                            commands.add(new MpCommand("=gg" + gw.g.asJson(), "all"));
                                        } else {
                                            commands.add(new MpCommand("=gf", "self"));
                                        }
                                    }
                                    catch (Exception e) {
                                        System.out.println("g post-process failed: " + e);
                                    }

                                }
                                else{
                                    // waiting to reach player limit
                                    commands.add(new MpCommand("=gw", "self"));
                                }
                            }
                            else{ // got a game running
                                commands.add(new MpCommand("=gg" + gw.g.asJson(), "self"));
                                commands.add(new MpCommand("=tr" + playerId, "others"));
                            }
                        }

                        // Broadcast commands if any
                        if(commands.size() > 0) {
                            MpHelpers.broadcastCommands(
                                    commands, // commands
                                    ctx, // current user's channel context
                                    (gw != null)? gw.broadcastList() : null // current game's broadcast list
                            );
                        }
                    }
                    catch (Exception e){
                        System.out.println("g command failed: " + e);
                    }
                }
                else {
                    // Any other command will be matched against game processors if game exists
                    // We will expect some kind of universal object so we can broadcast it
                    GameWrapper gw = Pool.gamesIndexedByPlayer.get(authorizedUser.getUsername());
                    if(gw.g != null) {
                        try {
                            Action resolution = GameManipulator.handleAction(
                                    gw.g, // game state
                                    gw.g.getColorByUsername(authorizedUser.getUsername()), // current user
                                    command, // command
                                    t.substring(2) // payload
                            );

                            MpHelpers.broadcastCommands(
                                    MpHelpers.convertEffectsToCommands(resolution.getEffects()), // commands
                                    ctx, // current user's channel context
                                    gw.broadcastList() // current game's broadcast list
                            );
                        }
                        catch (Exception e){
                            System.out.println("[WARN] Game resolution processing failed. " + e);
                        }
                    }
                }
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