package lv.dium.riskgame;

import lv.dium.riskgame.GameEffect;
import lv.dium.riskserver.MpCommand;

import java.util.ArrayList;

public class MpConvertor {
    /**
     * Converts GameEffect-s into MpCommand-s.
     * Null-safe.
     *
     * @param effects - effects to convert
     * @return - list of commands to broadcast
     */
    public static ArrayList<MpCommand> convertEffectsToCommands(ArrayList<GameEffect> effects) {
        ArrayList<MpCommand> commands = new ArrayList<>();

        StringBuilder commandLine = new StringBuilder("=f");

        try {
            if (effects != null && effects.size() > 0) {
                for (GameEffect gameEffect : effects) {
                    String nextCommand = gameEffect.getCommandLine();
                    String nextValue = gameEffect.getValues();

                    System.out.println("Command [" + nextCommand + "] with body [" + nextValue + "]");

                    if(nextCommand.length()>0){
                        commandLine.append(nextCommand).append(nextValue).append(".");
                    }
                }
                commands.add(new MpCommand(commandLine.toString(), "all"));
            } else {
                commands.add(new MpCommand("=err", "self"));
            }
        } catch (Exception e) {
            System.out.println("Failed formatting commands. " + e);
        }

        return commands;
    }

    /** Converts ={string} command into ArrayList<GameEffect> of verbose effects
     *
     * @param command - ={string} format command
     * @return - parsed effects
     */
    public static ArrayList<GameEffect> convertCommandToEffects(String command) {
        ArrayList<GameEffect> effects = new ArrayList<>();

        String[] effectsArray = command.split(".");

        for(String effectString: effectsArray) {
            if(effectString.length()>4){
                String action = effectString.substring(0,3);
                Integer areaId = Integer.valueOf(effectString.substring(5,7));
                String newValue = effectString.substring(3,5);

                effects.add(new GameEffect(action, areaId, newValue));
            }
            else{
                effects.add(new GameEffect(effectString));
            }
        }

        return effects;
    }
}
