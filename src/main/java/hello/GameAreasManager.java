package hello;

public class GameAreasManager {
    public Game game;

    public GameAreasManager(Game game){
        this.game = game;
    }

    public void moveUnits(Number sourceAreaID, Number targetAreaID, Number units) {
        Number newSrcStr = Integer.valueOf(game.getAreas().get(sourceAreaID.intValue()).getStr()) - units.intValue();
        Number newTrgStr = Integer.valueOf(game.getAreas().get(targetAreaID.intValue()).getStr()) + units.intValue();
        game.getAreas().get(sourceAreaID.intValue()).setStr(newSrcStr.toString());
        game.getAreas().get(targetAreaID.intValue()).setStr(newTrgStr.toString());
    }
}
