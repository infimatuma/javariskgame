package lv.dium.riskserver;

import java.util.HashMap;
import java.util.Map;

public class Pool {
    public static volatile Map<Number, Game> games = new HashMap<Number, Game>();
    public static volatile Map<String, Number> players = new HashMap<String, Number>();
}
