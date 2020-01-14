package lv.dium.riskserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class ScenarioArea {
    String x;
    String y;
    String str;
    String id;
    String color;
    ArrayList<Number> links;
    Scenario scenario;

    public void save(){
        try{
            String areaHashName = "SArea:"+scenario.getName()+":"+id;

            JedisConnection.getLink().hset(areaHashName, "x", x);
            JedisConnection.getLink().hset(areaHashName, "y", y);
            JedisConnection.getLink().hset(areaHashName, "str", str);
            JedisConnection.getLink().hset(areaHashName, "id", id);
            JedisConnection.getLink().hset(areaHashName, "color", color);

            String setName = "SAreaLinks:"+scenario.getName()+":"+id;
            JedisConnection.getLink().del(setName);
            if(links != null){
                Iterator linksIterator = links.iterator();
                while (linksIterator.hasNext()) {
                    Number linkId = (Number) linksIterator.next();
                    JedisConnection.getLink().sadd(setName, String.valueOf(linkId));
                }
            }
        }
        catch (Exception e){
            System.out.println("Failed to save ScenarioArea");
            System.out.println(e);
        }
    }

    public void load(){
        try{
            String areaHashName = "SArea:"+scenario.getName()+":"+id;

            x = JedisConnection.getLink().hget(areaHashName, "x");
            y = JedisConnection.getLink().hget(areaHashName, "y");
            str = JedisConnection.getLink().hget(areaHashName, "str");
            id = JedisConnection.getLink().hget(areaHashName, "id");
            color = JedisConnection.getLink().hget(areaHashName, "color");

            String setName = "SAreaLinks:"+scenario.getName()+":"+id;

            Set linksInRedis = JedisConnection.getLink().smembers(setName);
            links = new ArrayList<Number>();
            if(linksInRedis!=null && linksInRedis.size()>0) {
                Iterator setIterator = linksInRedis.iterator();
                while (setIterator.hasNext()) {
                    String nextVal = (String) setIterator.next();
                    links.add(Integer.valueOf(nextVal));
                }
            }
        }
        catch (Exception e){
            System.out.println("Failed to load ScenarioArea");
            System.out.println(e);
        }
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
