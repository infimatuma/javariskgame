package lv.dium.riskserver;

import lv.dium.riskgame.GameArea;

import static java.lang.Integer.parseInt;

public class Battle {
    public void resolve (GameArea attackingArea, int numberOfAttackers, GameArea defendingArea) {
        attackingArea.setStr(attackingArea.getStr() - numberOfAttackers);
        int numberOfDefenders = defendingArea.getStr();

// Defender won:
        defendingArea.setStr(numberOfDefenders);

    }
     public int rollDice() {
         return (int) (Math.random() * 6)+1;
    }
}
