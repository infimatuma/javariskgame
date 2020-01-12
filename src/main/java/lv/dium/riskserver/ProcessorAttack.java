package lv.dium.riskserver;

import java.util.ArrayList;

@RiskActionProcessor("a")
public class ProcessorAttack implements GameActionProcessor{
    private Integer targetAreaID;
    private Integer sourceAreaID;
    private Integer units;

    private void parsePayload(String payload){
        sourceAreaID = Integer.valueOf(payload.substring(0,2));
        targetAreaID = Integer.valueOf(payload.substring(2,4));
        units = Integer.valueOf(payload.substring(4,6));
    }

    @Override
    public ArrayList<GameEffect> resolve(GameState g, String payload){
        ArrayList<GameEffect> effects = new ArrayList<GameEffect>();

        try {
            /*
            parsePayload(gameActionHandler.resolution.getPayload());

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

                effects.add(new GameEffect("updateAreaStr", sourceAreaID.intValue(), String.valueOf(gameActionHandler.game.getAreas().get(sourceAreaID.intValue()).getStr())));
                effects.add(new GameEffect("updateAreaStr", targetAreaID.intValue(), String.valueOf(gameActionHandler.game.getAreas().get(targetAreaID.intValue()).getStr())));

                System.out.println("Processing attack");
                gameActionHandler.resolution.addGameEffects(effects);
            }
             */
        }
        catch (Exception e) {
            System.out.println("Failed to process ProcessorAttack!");
            System.out.println(e);
        }

        return effects;
    }

}
