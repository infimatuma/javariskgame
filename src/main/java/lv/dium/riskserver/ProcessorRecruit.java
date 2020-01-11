package lv.dium.riskserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RiskActionProcessor("r")
public class ProcessorRecruit implements GameActionProcessor{
    private GameActionHandler gameActionHandler;
    private Map<Integer, Integer> affectedAreas = new HashMap<>();
    private Integer assignedUnits = 0;

    private void parsePayload(String payload){
        String nextBlock;
        Integer nextBlockIndex = 0;
        Integer areaID;
        Integer units;

        try {
            if(payload.length()>=4) {
                nextBlock = payload.substring(4 * nextBlockIndex, 4 * nextBlockIndex + 4);
            }
            else{
                System.out.println("ProcessorRecruit, invalid payload!");
                nextBlock = null;
            }
            System.out.println("ProcessorRecruit, next block: " + nextBlock);
            while (nextBlock != null && (nextBlock.length() == 4)) {

                areaID = Integer.valueOf(nextBlock.substring(0, 2));
                units = Integer.valueOf(nextBlock.substring(2, 4));

                affectedAreas.put(areaID, units);
                assignedUnits += units;

                nextBlockIndex++;
                if(payload.length() >= 4 * nextBlockIndex + 4) {
                    nextBlock = payload.substring(4 * nextBlockIndex, 4 * nextBlockIndex + 4);
                    System.out.println("ProcessorRecruit, next block: " + nextBlock);
                }
                else{
                    System.out.println("ProcessorRecruit, no next block possible!" );
                    if(payload.length() >= 4 * nextBlockIndex){
                        System.out.println("Remaining data: " +  payload.substring(4 * nextBlockIndex));
                    }
                    nextBlock = null;
                }
            }

            if(nextBlock != null) {
                System.out.println("Last block = " + nextBlock);
            }

        }
        catch(Exception e){
            System.out.println("Failed to parsePayload ProcessorRecruit!");
            System.out.println(e);
        }
    }

    @Override
    public void resolve(){
        try {
            parsePayload(gameActionHandler.resolution.getPayload());

            if(affectedAreas.size() == 0){
                System.out.println("ERROR: ProcessorRecruit requires at least one assignment!");
            }
            else if(assignedUnits != gameActionHandler.game.countUnallocated_units()){
                System.out.println("ERROR: ProcessorRecruit ask for all units to be allocated in one command!");
            }
            else{
                ArrayList<GameEffect> effects = new ArrayList<GameEffect>();

                affectedAreas.forEach((Integer k, Integer v) ->{
                    Integer newStr = gameActionHandler.game.findAreasManager().addUnits(k, v);
                    effects.add(new GameEffect("recruit", k, String.valueOf(newStr)));
                });

                System.out.println("Processing recruit");
                gameActionHandler.resolution.addGameEffects(effects);
            }
        }
        catch (Exception e) {
            System.out.println("Failed to process ProcessorRecruit!");
            System.out.println(e);
        }
    }

    @Override
    public void setGameActionHandler(GameActionHandler gameActionHandler) {
        this.gameActionHandler = gameActionHandler;
    }
}