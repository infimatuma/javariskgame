package lv.dium.riskserver;

import java.util.ArrayList;

public class GameFullState {

    public GameFullState() {
    }

    public ArrayList<GameArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<GameArea> areas) {
        this.areas = areas;
    }

    ArrayList<GameArea> areas;

    public void createFromGame(Game game) {
        
    }
}
