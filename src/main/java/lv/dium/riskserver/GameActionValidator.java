package lv.dium.riskserver;

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
        else if(testedAction.getAction().equals("a")){
            // validate attack permissions
            if(!currentPhase.equals("attack")){
                System.out.println("Wrong currentPhase (need attack): " + currentPhase);
                isOk = false;
            }
        }
        else if(testedAction.getAction().equals("r")){
            // validate recruit permissions
            if(!currentPhase.equals("recruit")){
                System.out.println("Wrong currentPhase (need recruit): " + currentPhase);
                isOk = false;
            }
        }

        System.out.println("Action validation: " + String.valueOf(isOk));

        return isOk;
    }
}
