package lv.dium.riskgame;

import lv.dium.riskserver.Action;

import java.util.ArrayList;

public class GameActionValidator {

    /** Validates if gameState qualifies for switching to next phase
     *
     * @param gameState - full game state to validate
     * @return - true for valid, false otherwise
     */
    public static boolean validateSwitchPhase(GameState gameState){
        boolean isOK = true;

        if(gameState.getCurrentPhase().equals("recruit")){
            // must allocate all units before continue
            if(gameState.countUnallocated_units() > 0){
                isOK = false;
            }
        }

        return isOK;
    }

    /** validate if provided Action is valid at this point and should be processed
     *
     * @param g - GameState to test against
     * @param testedAction - Action to test
     * @return true for valid, false otherwise
     */
    public static boolean validateAction(GameState g, Action testedAction) {
        boolean isOk = true;

        String currentPhase = g.getCurrentPhase();
        String currentPlayerColor = g.findCurrentPlayerColor();

        if(!testedAction.getActingColor().equals(currentPlayerColor)){
            System.out.println("Wrong action player: " + testedAction.getActingColor() + " != " + currentPlayerColor);
            isOk = false;
        }
        else if(testedAction.getAction().equals("attk")){
            // validate attack permissions
            if(!currentPhase.equals("attack")){
                System.out.println("Wrong currentPhase (need attack): " + currentPhase);
                isOk = false;
            }
        }
        else if(testedAction.getAction().equals("rcrt")){
            // validate recruit permissions
            if(!currentPhase.equals("recruit")){
                System.out.println("Wrong currentPhase (need recruit): " + currentPhase);
                isOk = false;
            }
        }
        else if(testedAction.getAction().equals("rnfc")){
            // validate recruit permissions
            if(!currentPhase.equals("reinforce")){
                System.out.println("Wrong currentPhase (need reinforce): " + currentPhase);
                isOk = false;
            }
        }
        else if(testedAction.getAction().equals("swch")){
            // validate recruit permissions
            if(currentPhase.equals("recruit") && g.countUnallocated_units() > 0){
                System.out.println("Not all units allocated in phase " + currentPhase);
                isOk = false;
            }
        }

        System.out.println("Action validation: " + String.valueOf(isOk));

        return isOk;
    }

    public ArrayList<GameValidAction> listValidActions(GameState g){
        String currentPhase = g.getCurrentPhase();
        Integer currentPlayerIndex = g.getCurrentPlayerIndex();

        ArrayList<GameValidAction> validActions = new ArrayList<>();

        if(currentPhase.equals("setup")){
            validActions.add(new GameValidAction("swch", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
        }
        switch (currentPhase) {
            case "recruit":
                if(g.countUnallocated_units() > 0) {
                    validActions.add(new GameValidAction("rcrt", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                }
                else{
                    validActions.add(new GameValidAction("swch", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                }
                break;

            case "attack":
                validActions.add(new GameValidAction("attk", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                validActions.add(new GameValidAction("swch", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                break;

            case "reinforce":
                validActions.add(new GameValidAction("rnfc", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                validActions.add(new GameValidAction("swch", currentPlayerIndex, g.findColorByIndex(currentPlayerIndex)));
                break;
        }

        return validActions;
    }
}
