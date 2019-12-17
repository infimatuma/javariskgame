package hello;

/* Redis: player:{login} = currentGameId */

public class GamePlayer {
    Game game;
    String name;

    public GamePlayer(String name, Game game){
        this.game = game;
        this.name = name;


    }
}
