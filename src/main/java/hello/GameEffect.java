package hello;

public class GameEffect {
    private String action;
    private Integer areaID;
    private String newValue;

    public GameEffect(String action, Integer areaID, String newValue){
        this.action = action;
        this.areaID = areaID;
        this.newValue = newValue;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getAreaID() {
        return areaID;
    }

    public void setAreaID(Integer areaID) {
        this.areaID = areaID;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
