package hello;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

public class Scenario extends Action {

    ArrayList<GameScenatioArea> areas;
    public Jedis jedis;
    public String name;

    public Scenario(String name) {
        this.name = name;
    }

    public Scenario(String name, ArrayList<GameScenatioArea> areas) {
        this.areas = areas;
        this.name = name;
    }

    private void connect(){
        if(jedis == null){
            jedis = new Jedis("paris.cloudyhost.info", 6379);
            System.out.println("Autentification " + jedis.auth("Risk#777b&"));
            System.out.println("Server Ping: " + jedis.ping());
        }
    }

    public void save(){
        System.out.println("wanna save scenario ["+name+"]!");

        try {
            connect();

            jedis.sadd("risk.scenarios", name);
            jedis.hset("risk.scenario:"+name, "numberOfAreas", new String(""+areas.size()));

            Iterator areasIterator = areas.iterator();
            while (areasIterator.hasNext()) {
                GameScenatioArea currentArea = (GameScenatioArea) areasIterator.next();
                currentArea.setScenario(this);
                currentArea.save();
            }

        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
    public void load(){
        System.out.println("wanna load scenario ["+name+"]!");

        areas = new ArrayList<>();

        try {
            connect();

            String scenarioKey = "risk.scenario:"+name;
            //System.out.println(scenarioKey);
            String scenarioAreasCnt = jedis.hget("risk.scenario:"+name, "numberOfAreas");
            //System.out.println(scenarioAreasCnt);
            Number AreasSize = Integer.valueOf(scenarioAreasCnt);
            //System.out.println(AreasSize);

            IntStream.range(0, AreasSize.intValue()).forEach(i -> {
            //IntStream.range(0, 3).forEach(i -> {
                GameScenatioArea nextArea = new GameScenatioArea();
                nextArea.setId(String.valueOf(i));
                nextArea.setScenario(this);
                nextArea.load();
                areas.add(nextArea);
            });

        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void prepareForClient(){
        jedis = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<GameScenatioArea> getAreas() {
        return areas;
    }
}
