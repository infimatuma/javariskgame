package hello;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class GameScenatioArea {
    String x;
    String y;
    String str;
    String id;
    String color;
    ArrayList<Number> links;
    Scenario scenario;

    public void save(){
        String areaHashName = "SArea:"+scenario.getName()+":"+id;
        System.out.println("Save ["+areaHashName+"]");

        scenario.jedis.hset(areaHashName, "x", x);
        scenario.jedis.hset(areaHashName, "y", y);
        scenario.jedis.hset(areaHashName, "str", str);
        scenario.jedis.hset(areaHashName, "id", id);
        scenario.jedis.hset(areaHashName, "color", color);

        String setName = "SAreaLinks:"+scenario.getName()+":"+id;
        System.out.println("Save ["+setName+"]");
        scenario.jedis.del(setName);
        if(links != null){
            Iterator linksIterator = links.iterator();
            while (linksIterator.hasNext()) {
                Number linkId = (Number) linksIterator.next();
                scenario.jedis.sadd(setName, String.valueOf(linkId));
            }
        }
    }

    public void load(){
        String areaHashName = "SArea:"+scenario.getName()+":"+id;

        x = scenario.jedis.hget(areaHashName, "x");
        y = scenario.jedis.hget(areaHashName, "y");
        str = scenario.jedis.hget(areaHashName, "str");
        id = scenario.jedis.hget(areaHashName, "id");
        color = scenario.jedis.hget(areaHashName, "color");

        String setName = "SAreaLinks:"+scenario.getName()+":"+id;
        System.out.println("Get ["+setName+"]");

        Set linksInRedis = scenario.jedis.smembers(setName);
        links = new ArrayList<Number>();
        if(linksInRedis!=null && linksInRedis.size()>0) {
            Iterator setIterator = linksInRedis.iterator();
            while (setIterator.hasNext()) {
                String nextVal = (String) setIterator.next();
                links.add(Integer.valueOf(nextVal));
            }
        }
        System.out.println("go-next");
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

    public void setLinks(ArrayList<Number> links) {
        this.links = links;
    }

    public void setScenario(Scenario parentScenario) {
        this.scenario = parentScenario;
    }
}
