package lv.dium.riskgame;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

public class GameEffect {
    private String action;
    private Integer areaID;
    private String newValue;

    private final String commandLine;
    private String command = "";
    private String values = "";

    private Boolean isCosmetic = true;

    private static Map<String, Boolean> cosmetics = new HashMap<>();

    public static void initCosmetics(){
        cosmetics.put("attk", true); // attack
        cosmetics.put("rcrt", true); // recruit
        cosmetics.put("rnfc", true); // reinforce

        cosmetics.put("nstr", false);  // new str
        cosmetics.put("nclr", false);  // new color
    }
    public Boolean Cosmetic(){
        if(cosmetics.size() < 1){ // plan-B, not thread safe
            initCosmetics();
        }
        if(cosmetics.containsKey(action)){
            isCosmetic = cosmetics.get(action);
        }
        else{
            isCosmetic = true;
        }
        return isCosmetic;
    }
    public GameEffect(String action){
        this(action, 0, "");
    }
    public GameEffect(String action, Integer areaID, String newValue){
        this.action = action;
        this.areaID = areaID;
        this.newValue = newValue;
        Cosmetic();

        switch (action) {
            case "rcrt":
                command = action;
                values = "";
                break;

            case "attk":
            case "rnfc":
            case "nstr":
            case "nclr":
                command = action;
                values = AddArea() + AddValue();
                break;
        }

        commandLine = command;
    }
    public GameEffect(String commandLine, String values){
        this.commandLine = commandLine;
        this.values = values;

        command = commandLine;

        switch (command){
            case "rcrt":
                action = command;
                break;

            case "attk":
            case "nstr":
            case "nclr":
                action = command;
                parseFour();
                break;
        }

    }

    private String AddArea(){
        return Strings.padStart(String.valueOf(areaID), 2, '0');
    }

    private String AddValue(){
        return Strings.padStart(newValue, 2, '0');
    }

    private void parseFour(){
        areaID = Integer.valueOf(values.substring(0,2));
        newValue = values.substring(2,4);
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
