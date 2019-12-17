package hello;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

public class Scenario extends Action {

    private ArrayList<GameScenarioArea> areas;
    public String name;

    public Scenario(String name) {
        this.name = name;
    }

    public Scenario(String name, ArrayList<GameScenarioArea> areas) {
        this.areas = areas;
        this.name = name;
    }

    public void save(){
        System.out.println("wanna save scenario ["+name+"]!");

        try {
            JedisConnection.getLink().sadd("risk.scenarios", name);
            JedisConnection.getLink().hset("risk.scenario:"+name, "numberOfAreas", new String(""+areas.size()));

            Iterator areasIterator = areas.iterator();
            while (areasIterator.hasNext()) {
                GameScenarioArea currentArea = (GameScenarioArea) areasIterator.next();
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

            String scenarioKey = "risk.scenario:"+name;
            //System.out.println(scenarioKey);
            String scenarioAreasCnt = JedisConnection.getLink().hget("risk.scenario:"+name, "numberOfAreas");
            //System.out.println(scenarioAreasCnt);
            Number AreasSize = Integer.valueOf(scenarioAreasCnt);
            //System.out.println(AreasSize);

            IntStream.range(0, AreasSize.intValue()).forEach(i -> {
            //IntStream.range(0, 3).forEach(i -> {
                GameScenarioArea nextArea = new GameScenarioArea();
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

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<GameScenarioArea> getAreas() {
        return areas;
    }
}
