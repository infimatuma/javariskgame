package lv.dium.riskserver;

import java.util.ArrayList;

public class GameArea {
    String x;
    String y;
    String str;
    String id;
    String color;
    ArrayList<Number> links = new ArrayList<Number>();
    Game game;

    public GameArea(GameScenarioArea area, Game game) {
        this.x = area.x;
        this.y = area.y;
        this.str = area.str;
        this.id = area.id;
        this.color = area.color;
        this.game = game;
    }
    public GameArea(String id, Game game) {
        this.id = id;
        this.game = game;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<Number> getLinks() {
        return links;
    }
}
