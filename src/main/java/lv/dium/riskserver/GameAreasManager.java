package lv.dium.riskserver;

public class GameAreasManager {
    public Game game;

    public GameAreasManager(Game game){
        this.game = game;
    }

    public void moveUnits(Number sourceAreaID, Number targetAreaID, Number units) {
        int newSrcStr = game.getAreas().get(sourceAreaID.intValue()).getStr() - units.intValue();
        int newTrgStr = game.getAreas().get(targetAreaID.intValue()).getStr() + units.intValue();
        game.getAreas().get(sourceAreaID.intValue()).setStr(newSrcStr);
        game.getAreas().get(targetAreaID.intValue()).setStr(newTrgStr);
    }

    public Integer addUnits(Integer targetAreaID, Integer units) {
        Integer newTrgStr = game.getAreas().get(targetAreaID).getStr() + units;
        game.getAreas().get(targetAreaID).setStr(newTrgStr);
        return newTrgStr;
    }
}
