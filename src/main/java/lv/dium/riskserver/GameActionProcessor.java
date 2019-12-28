package lv.dium.riskserver;

public interface GameActionProcessor {
    GameActionHandler gameActionHandler = null;

    default void resolve(){

    }
    default void setGameActionHandler(GameActionHandler gameActionHandler){

    }
}
