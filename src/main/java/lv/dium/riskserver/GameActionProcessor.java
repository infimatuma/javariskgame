package lv.dium.riskserver;

import lv.dium.riskgame.GameEffect;
import lv.dium.riskgame.GameState;

import java.util.ArrayList;

public interface GameActionProcessor {
    ArrayList<GameEffect> resolve(GameState g, String payload);
}
