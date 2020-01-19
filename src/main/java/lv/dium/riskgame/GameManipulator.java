package lv.dium.riskgame;

import lv.dium.riskserver.Action;
import lv.dium.riskserver.GameActionProcessor;
import lv.dium.riskserver.JedisConnection;
import lv.dium.riskserver.RiskActionProcessors;
import lv.dium.riskserver.Scenario;
import lv.dium.riskserver.ScenarioArea;
import lv.dium.riskserver.WiredAction;

import java.util.ArrayList;

public class GameManipulator {

    /** Create blank game
     *
     * @return - returns link to gameState
     */
    public static GameState create(){
        return new GameState();
    }

    /** Start the game according to provided scenario
     *
     * @param g - gameState to manipulate
     * @param scenarioName - scenario name to base game onto
     * @return - returns link to altered gameState for convenience
     */
    public static GameState start(GameState g, String scenarioName){

        if (g.getId() == null && !scenarioName.equals("")) {
            try {
                g.setId(JedisConnection.getLink().incr("risk.gameIds").intValue());
            }
            catch (Exception e){
                System.out.println("Jedis error: " + e);
            }
            if (g.getId() != null) {

                g.setCurrentPhase("setup");
                g.setCurrentPlayer("waiting-for-players");

                Scenario baseScenario = new Scenario(scenarioName);
                try {
                    baseScenario.load();

                    ArrayList<ScenarioArea> scenarioAreas = baseScenario.getAreas();
                    for (ScenarioArea scenarioArea : scenarioAreas) {
                        if (scenarioArea != null) {
                            GameArea newArea = new GameArea(
                                    scenarioArea.getX(),
                                    scenarioArea.getY(),
                                    Integer.valueOf(scenarioArea.getStr()),
                                    Integer.valueOf(scenarioArea.getId()),
                                    scenarioArea.getColor(),
                                    scenarioArea.getLinks()
                            );
                            g.getAreas().add(newArea);
                        }
                    }

                    g.randomStart();

                } catch (Exception e) {
                    System.out.println("Failed to create game[" + g.getId() + "]");
                }
            }
        }

        return g;
    }

    /**  Switch phase to next
     * ONLY performs switch, no validation done here
     * */
    public static GameState switchToNextPhase(GameState g){
        String currentPhase = g.getCurrentPhase();
        Integer maxPlayers = g.getMaxPlayers();
        Integer currentPlayerIndex = g.getCurrentPlayerIndex();

        if(currentPhase.equals("setup")){
            if(maxPlayers > currentPlayerIndex + 1) {
                currentPlayerIndex++;
            }
            else{
                currentPhase = "recruit";
                currentPlayerIndex = 0;
            }
        }
        switch (currentPhase) {
            case "recruit":
                currentPhase = "attack";
                break;

            case "attack":
                currentPhase = "reinforce";
                break;

            case "reinforce":
                if (maxPlayers > currentPlayerIndex + 1) {
                    currentPlayerIndex++;
                } else {
                    currentPlayerIndex = 0;
                }
                currentPhase = "recruit";
                break;
        }

        g.setCurrentPhase(currentPhase);
        g.setCurrentPlayerIndex(currentPlayerIndex);

        return g;
    }

    public static Action handleAction(GameState g, String actingColor, String action, String payload) {
        // create basic Action out of message
        Action resolution = new Action(action, payload);
        System.out.println("Action instant = " + action + " [ " + payload + "]");

        /* no actions accepted until game is already running
        We always expect !g greetings before any actions
        We will force re-login if no active game state exists
         */

        if (g.getId() != null && action != null) {
            /* should have id set now */
            /* lock the game before we load it */

            g.lock.lock();
            try {
                resolution.setActingColor(actingColor);

                if(GameActionValidator.validateAction(g, resolution)) {
                    ArrayList<GameEffect> effects = GameManipulator.processAction(g, resolution);
                    GameManipulator.applyEffects(g, effects);
                    resolution.setEffects(effects);
                }

            } catch (Exception e) {
                System.out.println("Failed to process game[" + g.getId() + "] action[" + action + "]");
            } finally {
                g.lock.unlock();
            }
        }

        /* return complete Action (should be broadcasted in controller) */
        return resolution;
    }


    public static ArrayList<GameEffect> processAction(GameState g, Action currentAction){
        ArrayList<GameEffect> effects = new ArrayList<>();
        System.out.println("Will process [" + currentAction.getAction() + "] ");

        try {
            ArrayList<WiredAction> wiredActions = RiskActionProcessors.getWiredActions();
            for (WiredAction wiredAction : wiredActions) {
                if (currentAction.getAction().equals(wiredAction.getActionName())) {
                    try {
                        System.out.println("Got processor: [" + wiredAction.getClassName() + "] for action [" + wiredAction.getActionName() + "] ");
                        Class<?> clazz = Class.forName("lv.dium.riskserver." + wiredAction.getClassName());
                        GameActionProcessor myActionProcessor = (GameActionProcessor) clazz.newInstance();
                        effects = myActionProcessor.resolve(g, currentAction.getPayload());
                    } catch (Exception e) {
                        System.out.println("Reflection magic failure! Call priests!" + e);
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("Failed processAction: " + e);
        }

        return effects;
    }


    public static void applyEffect(GameState g, GameEffect e){
        String action = e.getAction();

        if(action.equals("nstr")){
            g.getAreas().get(e.getAreaID()).setStr(Integer.valueOf(e.getNewValue()));
        }
        else if(action.equals("nclr")){
            g.getAreas().get(e.getAreaID()).setColor(e.getNewValue());
        }
    }

    public static void applyEffects(GameState g, ArrayList<GameEffect> effects){
        effects.forEach((GameEffect e) -> GameManipulator.applyEffect(g, e));
    }


}
