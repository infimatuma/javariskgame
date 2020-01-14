package lv.dium.riskserver;


import lv.dium.riskgame.GameEffect;

import java.util.ArrayList;

/* Action is a set of data broadcasted to all players as a result of new player action received by server
*
* */
public class Action {

    private String action;
    private String payload;
    private String actingColor;
    private ArrayList<GameEffect> effects;

    public Action() {
    }

    public Action(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public void addGameEffect(GameEffect effect){
        if(effects == null){
            effects = new ArrayList<>();
        }
        effects.add(effect);
    }
    public void addGameEffects(ArrayList<GameEffect> effects){
        effects.forEach(this::addGameEffect);
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    public ArrayList<GameEffect> getEffects() {
        return effects;
    }

    public void setEffects(ArrayList<GameEffect> effects) {
        this.effects = effects;
    }

    public String getActingColor() {
        return actingColor;
    }

    public void setActingColor(String actingColor) {
        this.actingColor = actingColor;
    }

}