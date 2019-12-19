package hello;

import java.util.ArrayList;

public class ProcessorAttack {
    private GameActionHandler gameActionHandler;

    public ProcessorAttack(GameActionHandler gameActionHandler) {
        this.gameActionHandler = gameActionHandler;
    }
    public void resolve(){
        Number targetAreaID = Integer.valueOf(gameActionHandler.resolution.getTargetArea());
        Number sourceAreaID = Integer.valueOf(gameActionHandler.resolution.getArea());
        Number units = gameActionHandler.resolution.getUnits();

        gameActionHandler.game.findAreasManager().moveUnits(sourceAreaID, targetAreaID, units);

        ArrayList<GameEffect> effects = new  ArrayList<GameEffect>();

        effects.add(new GameEffect("updateAreaStr", sourceAreaID.intValue(), gameActionHandler.game.getAreas().get(sourceAreaID.intValue()).getStr()));
        effects.add(new GameEffect("updateAreaStr", targetAreaID.intValue(), gameActionHandler.game.getAreas().get(targetAreaID.intValue()).getStr()));

        System.out.println("Processing attack");
        gameActionHandler.resolution.addGameEffects(effects);
    }
}
