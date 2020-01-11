package lv.dium.riskserver;

import com.google.common.base.Strings;

public class GameEffect {
    private String action;
    private Integer areaID;
    private String newValue;

    private String command;
    private String values;

    public GameEffect(String action, Integer areaID, String newValue){
        this.action = action;
        this.areaID = areaID;
        this.newValue = newValue;

        if(action.equals("recruit")){
            command = "=r";
            values = Strings.padStart(String.valueOf(areaID), 2, '0') + Strings.padStart(String.valueOf(newValue), 2, '0');
        }
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

    public String getCommand() {
        return command;
    }

    public String getValues() {
        return values;
    }
}
