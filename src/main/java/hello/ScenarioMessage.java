package hello;

import java.util.ArrayList;

public class ScenarioMessage {
    String name;
    ArrayList<GameScenatioArea> areas;

    public ArrayList<GameScenatioArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<GameScenatioArea> areas) {
        this.areas = areas;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
