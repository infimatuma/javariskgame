package hello;

/* Redis: player:{login} = currentGameId */

public class GamePlayer {
    Game game;
    String name;

    public GamePlayer(Game game, String name){
        this.game = game;
        this.name = name;


    }
}
