package lv.dium.riskgame;

public class GameValidAction {
    public String color;
    public String action;
    public Integer playerIndex;
    public Integer srcAreadId = -1;
    public Integer trgAreadId = -1;
    public Integer maxUnits = 0;

    public GameValidAction(String action, Integer playerIndex, String color){
        this.action = action;
        this.playerIndex = playerIndex;
        this.color = color;
    }

    public GameValidAction(String action, Integer playerIndex, String color, Integer srcAreadId, Integer trgAreadId, Integer maxUnits){
        this.action = action;
        this.playerIndex = playerIndex;
        this.color = color;
        this.srcAreadId = srcAreadId;
        this.trgAreadId = trgAreadId;
        this.maxUnits = maxUnits;
    }
}
