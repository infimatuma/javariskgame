package hello;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class GameArea {
    String x;
    String y;
    String str;
    String id;
    String color;
    ArrayList<Number> links;
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
    public GameArea save(){
        String areaHashName = "GArea:"+game.getId()+":"+id;
        System.out.println("Save ["+areaHashName+"]");

        JedisConnection.getLink().hset(areaHashName, "x", x);
        JedisConnection.getLink().hset(areaHashName, "y", y);
        JedisConnection.getLink().hset(areaHashName, "str", str);
        JedisConnection.getLink().hset(areaHashName, "id", id);
        JedisConnection.getLink().hset(areaHashName, "color", color);
        return this;
    }

    public GameArea load(){
        String areaHashName = "GArea:"+game.getId()+":"+id;

        x = JedisConnection.getLink().hget(areaHashName, "x");
        y = JedisConnection.getLink().hget(areaHashName, "y");
        str = JedisConnection.getLink().hget(areaHashName, "str");
        id = JedisConnection.getLink().hget(areaHashName, "id");
        color = JedisConnection.getLink().hget(areaHashName, "color");

        String setName = "SAreaLinks:"+game.getScenarioName()+":"+id;
        System.out.println("Get ["+setName+"]");

        Set linksInRedis = JedisConnection.getLink().smembers(setName);
        links = new ArrayList<Number>();
        if(linksInRedis!=null && linksInRedis.size()>0) {
            Iterator setIterator = linksInRedis.iterator();
            while (setIterator.hasNext()) {
                String nextVal = (String) setIterator.next();
                links.add(Integer.valueOf(nextVal));
            }
        }
        return this;
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
}
