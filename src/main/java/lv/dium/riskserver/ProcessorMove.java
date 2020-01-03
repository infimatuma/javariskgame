package lv.dium.riskserver;

import java.util.ArrayList;

@RiskActionProcessor("m")
public class ProcessorMove implements GameActionProcessor{
    private GameActionHandler gameActionHandler;
    private Integer targetAreaID;
    private Integer sourceAreaID;
    private Integer units;

    private void parsePayload(String payload){
        sourceAreaID = Integer.valueOf(payload.substring(0,2));
        targetAreaID = Integer.valueOf(payload.substring(2,4));
        units = Integer.valueOf(payload.substring(4,6));
    }

    @Override
    public void resolve(){
        try {
            parsePayload(gameActionHandler.resolution.getPayload());

            if(targetAreaID == null){
                System.out.println("ERROR: ProcessorMove requires targetAreaID!");
            }
            else if(sourceAreaID == null){
                System.out.println("ERROR: ProcessorMove requires sourceAreaID!");
            }
            else if(units == null) {
                System.out.println("ERROR: ProcessorMove requires units!");
            }
            else{
                gameActionHandler.game.findAreasManager().moveUnits(sourceAreaID, targetAreaID, units);

                ArrayList<GameEffect> effects = new ArrayList<GameEffect>();

                effects.add(new GameEffect("updateAreaStr", sourceAreaID.intValue(), gameActionHandler.game.getAreas().get(sourceAreaID.intValue()).getStr()));
                effects.add(new GameEffect("updateAreaStr", targetAreaID.intValue(), gameActionHandler.game.getAreas().get(targetAreaID.intValue()).getStr()));

                System.out.println("Processing Move");
                gameActionHandler.resolution.addGameEffects(effects);
            }
        }
        catch (Exception e) {
            System.out.println("Failed to process ProcessorMove!");
            System.out.println(e);
        }
    }

    @Override
    public void setGameActionHandler(GameActionHandler gameActionHandler) {
        this.gameActionHandler = gameActionHandler;
    }
}
