package hello;


import java.util.ArrayList;

/* Action is a set of data broadcasted to all players as a result of new player action received by server
*
* */
public class Action {

    private String action;
    private String area;
    private String targetArea;
    private Number units;
    private String playerId;
    private Boolean isValid;
    private ArrayList<GameEffect> effects;

    public Action() {
    }

    public Action(String playerId, String action, String area, String targetArea, Number units) {
        this.playerId = playerId;
        this.action = action;
        this.area = area;
        this.targetArea = targetArea;
        this.units = units;
        this.isValid = true;
    }

    public void addGameEffect(GameEffect effect){
        if(effects == null){
            effects = new ArrayList<GameEffect>();
        }
        effects.add(effect);
    }
    public void addGameEffects(ArrayList<GameEffect> effects){
        effects.forEach((n) -> this.addGameEffect(n));
    }


    public String getAction() {
        return action;
    }
    public String getArea() {
        return area;
    }
    public String getTargetArea() {
        return targetArea;
    }
    public Number getUnits() {
        return units;
    }
    public String getPlayerId() {
        return playerId;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public ArrayList<GameEffect> getEffects() {
        return effects;
    }

    public void setEffects(ArrayList<GameEffect> effects) {
        this.effects = effects;
    }
}