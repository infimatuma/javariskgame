package lv.dium.riskserver;

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
    public GamePlayer(String id, Game game){
        this.game = game;
        this.id = id;
    }

    public GamePlayer pickColor(){
        color = game.getFreeColor();
        return this;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
