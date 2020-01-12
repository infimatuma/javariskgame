package lv.dium.riskserver;

import java.util.ArrayList;

public interface GameActionProcessor {
    ArrayList<GameEffect> resolve(GameState g, String payload);
}
