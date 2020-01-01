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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handler implementation for the HexWar server.
 */
public class MpServerHandler extends SimpleChannelInboundHandler<String> {
    static final ChannelGroup afkChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final ChannelGroup authorizedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private LoginHandler loginHandler;
    static final LinkedBlockingQueue<MpUser> usersInQueue = new LinkedBlockingQueue<>();

    static final int maximumPlayersPerGame = 2;

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

        afkChannels.add(ctx.channel());

        // filter commands
        if(t.substring(0,1).equals("!")){

            MpUser authorizedUser = null;

            // login command
            if(t.substring(1,2).equals("l")){
                // payload should be a json string, but we do not test it here
                String payload = t.substring(2, t.length());

                MpUser user = loginHandler.getUserFromPayload(payload);
                if(user.isAuthorized()){
                    // add channel to authorized channels
                    authorizedUser = user;
                    authorizedChannels.add(ctx.channel());

                    ctx.channel().attr(MP_USER_ATTRIBUTE_KEY).set(user);
                    ChannelHandlerContext oldCtx = Pool.putAndGetUserChannel(user.getUsername(), ctx);

                    if(oldCtx == null){
                        System.out.println("New user login");
                    }
                    else{
                        System.out.println("User re-login");
                    }

                    // If ongoing game exists
                    Game existingGame = Pool.gamesIndexedByPlayer.get(user.getUsername());

                    // If old channel for the same user exists - close it
                    if(oldCtx != null){
                        System.out.println("Will match connectons");
                        // only close it if this is a different channel
                        if(oldCtx.channel().id() != ctx.channel().id()) {
                            System.out.println("Will close old connection");
                            if(existingGame != null) {
                                System.out.println("We have a game - so will call replaceOrSetChannel");
                                existingGame.replaceOrSetChannel(oldCtx.channel(), ctx.channel());
                            }
                            System.out.println("Writingf [fc] to old channel and closing");
                            oldCtx.channel().writeAndFlush("=fcNewChannel" + '\n');
                            oldCtx.channel().close();
                        }
                        else{
                            System.out.println("This is the same connection: " + oldCtx.channel().id() + " == " +ctx.channel().id());
                        }
                    }

                    if(existingGame != null){
                        // return login succeed, ongoing game exists
                        ctx.channel().writeAndFlush("=lg" + '\n');
                    }
                    else {
                        // return login succeed
                        ctx.channel().writeAndFlush("=ls" + '\n');
                    }
                }
                else{
                    // return login failed
                   ctx.channel().writeAndFlush("=lf" + '\n');
                }
            }
            // Authorization required for commands below
            if(authorizedUser == null) {
                authorizedUser = ctx.channel().attr(MP_USER_ATTRIBUTE_KEY).get();
            }

            if(authorizedUser != null && authorizedUser.isAuthorized()) {
                // greeting command
                if (t.substring(1, 2).equals("g")) {
                    try {
                        String playerNick = t.substring(2, t.length());

                        String playerId = authorizedUser.getUsername();

                        Game game = null;
                        String payload = "f";
                        Number gameId = null;

                        synchronized (this) {
                            try {
                                game = Pool.gamesIndexedByPlayer.get(playerId);
                            }
                            catch (Exception e){
                                System.out.println("Pool.player.get failed: " + e);
                            }

                            if (game == null) { // handle queue pool
                                if(!usersInQueue.contains(authorizedUser)) {
                                    usersInQueue.add(authorizedUser);
                                    System.out.println("Pool.add: " + authorizedUser.getUsername());
                                }

                                // Create a game if enough users
                                if (usersInQueue.size() >= maximumPlayersPerGame) {
                                    game = new Game();

                                    // create game's channel group
                                    DefaultChannelGroup gameChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                                    game.setChannelGroup(gameChannelGroup);

                                    try {
                                        for (int i = 0; i < maximumPlayersPerGame; i++) {
                                            MpUser nextUser = usersInQueue.poll();
                                            game.addUser(nextUser);

                                            // add users most recent channel to game's channel group
                                            gameChannelGroup.add(Pool.putAndGetUserChannel(nextUser.getUsername(), null).channel());

                                            try{
                                                Pool.gamesIndexedByPlayer.put(nextUser.getUsername(), game);
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
                                        game.start();
                                    }
                                    catch (Exception e){
                                        System.out.println("Game start failed: " + e);
                                    }

                                    gameId = game.getId();
                                    if (gameId != null) {

                                    }

                                    // We either got a running game now or something went wrong
                                    try {
                                        if (game != null) {
                                            payload = game.asJson();

                                            // broadcast game start to all users
                                            for (Channel c : game.broadcastBlockingList()) {
                                                System.out.println("Broadcast to game["+game.getId()+" channel[" + c.id() + "]: gg");
                                                c.writeAndFlush("=gg" + payload + '\n');
                                            }
                                        } else {
                                            ctx.channel().writeAndFlush("=g" + payload + '\n');
                                        }
                                    }
                                    catch (Exception e) {
                                        System.out.println("g post-process failed: " + e);
                                    }

                                }
                                else{
                                    // waiting to reach player limit
                                    payload = "w";
                                }
                            }
                            else{
                                // got a game running
                                payload = game.asJson();

                                // broadcast game start to all users
                                for (Channel c : game.broadcastBlockingList()) {
                                    if(c == ctx.channel()) {
                                        System.out.println("Point-transmit to game["+game.getId()+" channel[" + ctx.channel().id() + "]: gg");
                                        ctx.channel().writeAndFlush("=gg" + payload + '\n');
                                    }
                                    else{
                                        System.out.println("Broadcast to game[" + game.getId() + " channel[" + c.id() + "]: player reconnected [" + playerId + "]");
                                        c.writeAndFlush("=tr" + playerId + '\n');
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e){
                        System.out.println("g command failed: " + e);
                    }
                }

                if (t.substring(1, 2).equals("a")) {

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