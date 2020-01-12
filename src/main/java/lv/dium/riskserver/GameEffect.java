package lv.dium.riskserver;

import com.google.common.base.Strings;

public class GameEffect {
    private String action;
    private Integer areaID;
    private String newValue;

    private String commandLine;
    private String command;
    private String values;

    public GameEffect(String action, Integer areaID, String newValue){
        this.action = action;
        this.areaID = areaID;
        this.newValue = newValue;

        if(action.equals("recruit")){
            command = "r";
            commandLine = "=" + command;
            values = Strings.padStart(String.valueOf(areaID), 2, '0') + Strings.padStart(String.valueOf(newValue), 2, '0');
        }
    }
    public GameEffect(String commandLine, String values){
        this.commandLine = commandLine;
        this.values = values;

        command = commandLine.substring(1,2);

        if(command.equals("r")){
            action = "recruit";
            areaID = Integer.valueOf(values.substring(0,2));
            newValue = values.substring(2,4);
        }
    }

    public String getAction() {
        return action;
    }

    public Integer getAreaID() {
        return areaID;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public String getValues() {
        return values;
    }
}
