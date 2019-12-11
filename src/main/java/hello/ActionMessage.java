package hello;

public class ActionMessage {

    private String action;
    private String area;
    private String targetArea;
    private String playerId;
    private Number units;

    public ActionMessage(String playerId, String action, String area, String targetArea, Number units) {
        this.playerId = playerId;
        this.action = action;
        this.area = area;
        this.targetArea = targetArea;
        this.units = units;
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
}