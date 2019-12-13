package hello;

import java.util.ArrayList;

public class GameFullState {
    public ArrayList<GameArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<GameArea> areas) {
        this.areas = areas;
    }

    ArrayList<GameArea> areas;

}
