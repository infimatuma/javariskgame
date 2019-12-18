package hello;

/* Redis: player:{login} = currentGameId */

public class GamePlayer {
    Game game;
    String name;
    String color;

    public GamePlayer(String name, Game game){
        this.game = game;
        this.name = name;
    }

    public GamePlayer pickColor(){
        color = game.getFreeColor();
        return this;
    }

    public GamePlayer save(){
        String playerHashName = "GPlayer:"+game.getId()+":"+name;
        System.out.println("Save ["+playerHashName+"]");

        JedisConnection.getLink().hset(playerHashName, "color", color);
        return this;
    }
}
