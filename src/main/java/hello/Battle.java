package hello;

import static java.lang.Integer.parseInt;

public class Battle {
    public void resolve (GameArea attackingArea, int numberOfAttackers, GameArea defendingArea) {
        attackingArea.setStr(String.valueOf(parseInt(attackingArea.getStr()) - numberOfAttackers));
        int numberOfDefenders = parseInt(defendingArea.getStr());


// Defender won:
        defendingArea.setStr(String.valueOf(numberOfDefenders));

    }
     public int rollDice() {
         return (int) (Math.random() * 6)+1;
    }
}
