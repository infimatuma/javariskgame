package lv.dium.riskgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class GameArea {
    private String x;
    private String y;
    private Integer str;
    private Integer id;
    private String color;
    private ArrayList<Number> links = new ArrayList<>();

    @JsonCreator
    public GameArea(@JsonProperty("x") String x, @JsonProperty("y") String y, @JsonProperty("str") Integer str, @JsonProperty("id") Integer id, @JsonProperty("color") String color, @JsonProperty("links") ArrayList<Number> links) {
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

    public void addStr(int addedValue) {
        this.setStr(this.getStr() + addedValue);
    }

    public void setStr(Integer str) {
        this.str = str;
    }

    public void setLinks(ArrayList<Number> links) {
        this.links = links;
    }
}
