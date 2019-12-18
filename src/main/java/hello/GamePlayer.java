package hello;

/* Redis: player:{login} = currentGameId */

public class GamePlayer {
    Game game;
    String name;
    String color;
    String id;

    public GamePlayer(String name, String id, Game game){
        this.game = game;
        this.name = name;
        this.id = id;
    }

    public GamePlayer pickColor(){
        color = game.getFreeColor();
        return this;
    }

    public GamePlayer save(){
        String playerHashName = "GPlayer:"+game.getId()+":"+id;
        System.out.println("Save ["+playerHashName+"]");

        JedisConnection.getLink().hset(playerHashName, "color", color);
        JedisConnection.getLink().hset(playerHashName, "id", id);
        JedisConnection.getLink().hset(playerHashName, "name", name);

        return this;
    }

    public GamePlayer load(){
        String playerHashName = "GPlayer:"+game.getId()+":"+id;
        System.out.println("Load ["+playerHashName+"]");

        color = JedisConnection.getLink().hget(playerHashName, "color");
        name = JedisConnection.getLink().hget(playerHashName, "name");
        id = JedisConnection.getLink().hget(playerHashName, "id");

        return this;
    }
}
