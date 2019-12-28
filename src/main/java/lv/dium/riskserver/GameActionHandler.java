package lv.dium.riskserver;

import java.util.ArrayList;

public class GameActionHandler {
    public Game game;
    public Action resolution;

    public GameActionHandler(Game game, Action resolution) {
        this.game = game;
        this.resolution = resolution;
    }

    public void process(){
        System.out.println("Will process [" + resolution.getAction() + "] ");

        ArrayList<WiredAction> wiredActions = RiskActionProcessors.getWiredActions();
        wiredActions.forEach((WiredAction wiredAction) ->{
            if(resolution.getAction().equals(wiredAction.getActionName())){
                try{
                    System.out.println("Got processor: [" + wiredAction.getClassName() + "] for action [" + wiredAction.getActionName() + "] ");
                    Class<?> clazz = Class.forName("hello." + wiredAction.getClassName());
                    GameActionProcessor myActionProcessor = (GameActionProcessor) clazz.newInstance();
                    myActionProcessor.setGameActionHandler(this);
                    myActionProcessor.resolve();
                }
                catch (Exception e){
                    System.out.println("Reflection magic failure! Call priests!");
                    System.out.println(e);
                }
            }
        });
    }

    public Game getGame() {
        return game;
    }

    public Action getResolution() {
        return resolution;
    }
}
