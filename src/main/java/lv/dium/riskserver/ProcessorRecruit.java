package lv.dium.riskserver;

import lv.dium.riskgame.GameEffect;
import lv.dium.riskgame.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RiskActionProcessor("rcrt")
public class ProcessorRecruit implements GameActionProcessor{
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
    public ArrayList<GameEffect> resolve(GameState g, String payload){
        ArrayList<GameEffect> effects = new ArrayList<GameEffect>();

        try {
            parsePayload(payload);

            if(affectedAreas.size() == 0){
                System.out.println("ERROR: ProcessorRecruit requires at least one assignment!");
            }
            else if(assignedUnits != g.countUnallocated_units()){
                System.out.println("ERROR: ProcessorRecruit ask for all units to be allocated in one command!");
            }
            else{
                effects.add(new GameEffect("rcrt"));
                affectedAreas.forEach((Integer k, Integer v) ->{
                    Integer newStr = g.countAreaStr(k) + v;
                    effects.add(new GameEffect("nstr", k, String.valueOf(newStr)));
                });
                System.out.println("Processed recruit");
            }
        }
        catch (Exception e) {
            System.out.println("Failed to process ProcessorRecruit!");
            System.out.println(e);
        }

        return effects;
    }
}
