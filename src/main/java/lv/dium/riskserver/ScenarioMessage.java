package lv.dium.riskserver;

import java.util.ArrayList;

public class ScenarioMessage {
    String name;
    ArrayList<GameScenarioArea> areas;

    public ArrayList<GameScenarioArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<GameScenarioArea> areas) {
        this.areas = areas;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
