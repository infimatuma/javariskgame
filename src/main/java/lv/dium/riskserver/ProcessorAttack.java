package lv.dium.riskserver;

import java.util.ArrayList;

@RiskActionProcessor("attack")
public class ProcessorAttack implements GameActionProcessor{
    private GameActionHandler gameActionHandler;

    @Override
    public void resolve(){
        try {
            Number targetAreaID = Integer.valueOf(gameActionHandler.resolution.getTargetArea());
            Number sourceAreaID = Integer.valueOf(gameActionHandler.resolution.getArea());
            Number units = gameActionHandler.resolution.getUnits();

            if(targetAreaID == null){
                System.out.println("ERROR: ProcessorAttack requires targetAreaID!");
            }
            else if(sourceAreaID == null){
                System.out.println("ERROR: ProcessorAttack requires sourceAreaID!");
            }
            else if(units == null) {
                System.out.println("ERROR: ProcessorAttack requires units!");
            }
            else{
                gameActionHandler.game.findAreasManager().moveUnits(sourceAreaID, targetAreaID, units);

                ArrayList<GameEffect> effects = new ArrayList<GameEffect>();

                effects.add(new GameEffect("updateAreaStr", sourceAreaID.intValue(), gameActionHandler.game.getAreas().get(sourceAreaID.intValue()).getStr()));
                effects.add(new GameEffect("updateAreaStr", targetAreaID.intValue(), gameActionHandler.game.getAreas().get(targetAreaID.intValue()).getStr()));

                System.out.println("Processing attack");
                gameActionHandler.resolution.addGameEffects(effects);
            }
        }
        catch (Exception e) {
            System.out.println("Failed to process ProcessorAttack!");
            System.out.println(e);
        }
    }

    @Override
    public void setGameActionHandler(GameActionHandler gameActionHandler) {
        this.gameActionHandler = gameActionHandler;
    }
}
