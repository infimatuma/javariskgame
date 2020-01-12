package lv.dium.riskserver;

public class GamePlayer {
    final String name;
    final String color;
    final Integer id;

    public GamePlayer(String name, Integer id, String color){
        this.color = color;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Integer getId() {
        return id;
    }

}
