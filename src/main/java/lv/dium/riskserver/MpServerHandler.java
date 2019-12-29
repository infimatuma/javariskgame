package lv.dium.riskserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
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
    static final LinkedBlockingQueue<String> usersInQueue = new LinkedBlockingQueue<>();

    static final int maximumPlayersPerGame = 2;

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

            // login command
            if(t.substring(1,2).equals("l")){
                // payload should be a json string, but we do not test it here
                String payload = t.substring(2, t.length());

                MpUser user = loginHandler.getUserFromPayload(payload);
                if(user.isAuthorized()){
                    // add channel to authorized channels
                    authorizedChannels.add(ctx.channel());
                    Pool.channelUsers.put(ctx.channel().id().toString(), user);
                    Pool.userChannels.put(user.getUsername(), ctx.channel());
                    Pool.users.put(user.getUsername(), user);

                    // If ongoing game exists
                    if(Pool.players.get(user.getUsername()) != null){
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
            MpUser authorizedUser = Pool.channelUsers.get(ctx.channel().id().toString());
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
                                gameId = Pool.players.get(playerId);
                            }
                            catch (Exception e){
                                System.out.println("Pool.player.get failed: " + e);
                            }
                            if (gameId == null) { // handle queue pool
                                if(!usersInQueue.contains(authorizedUser.getUsername())) {
                                    usersInQueue.add(authorizedUser.getUsername());
                                }

                                // Create a game if enough users
                                if (usersInQueue.size() >= maximumPlayersPerGame) {
                                    game = new Game();

                                    // create game's channel group
                                    DefaultChannelGroup gameChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

                                    try {
                                        for (int i = 0; i < maximumPlayersPerGame; i++) {
                                            String nextUserLogin = usersInQueue.poll();
                                            game.addUser(Pool.users.get(nextUserLogin));
                                            gameChannelGroup.add(Pool.userChannels.get(nextUserLogin));
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
                                        try{
                                            Pool.gameChannelGroups.put(gameId, gameChannelGroup);
                                            Pool.games.put(gameId, game);
                                            Pool.players.put(playerId, gameId);
                                        }
                                        catch (Exception e){
                                            System.out.println("Pool TRIO failed: " + e);
                                        }
                                    }
                                }
                                else{
                                    // waiting to reach player limit
                                    payload = "w";
                                }
                            }
                            else{
                                try {
                                    game = Pool.games.get(gameId);
                                }
                                catch (Exception e){
                                    System.out.println("g Pool.games.get failed: " + e);
                                }
                            }
                        }
                        // We either got a running game now or something went wrong
                        try {
                            if (game != null) {
                                payload = game.asJson();

                                // broadcast game start to all users
                                ChannelGroup gameChannels = Pool.gameChannelGroups.get(game.getId());
                                for (Channel c : gameChannels) {
                                    c.writeAndFlush("=gg" + payload + '\n');
                                }
                            } else {
                                ctx.channel().writeAndFlush("=g" + payload + '\n');
                            }
                        }
                        catch (Exception e){
                            System.out.println("g post-process failed: " + e);
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