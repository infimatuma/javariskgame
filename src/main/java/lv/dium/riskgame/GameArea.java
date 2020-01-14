package lv.dium.riskgame;

import java.util.ArrayList;

public class GameArea {
    String x;
    String y;
    Integer str;
    Integer id;
    String color;
    ArrayList<Number> links = new ArrayList<>();

    public GameArea(String x, String y, Integer str, Integer id, String color, ArrayList<Number> links) {
        this.x = x;
        this.y = y;
        this.str = str;
        this.id = id;
        this.color = color;
        this.links = links;
    }

    public GameArea(Integer id) {
        this.id = id;
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

    public int getStr() {
        return str;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setStr(int str) {
        this.str = str;
    }

    public void addStr(int addedValue) {
        this.setStr(this.getStr() + addedValue);
    }
}
