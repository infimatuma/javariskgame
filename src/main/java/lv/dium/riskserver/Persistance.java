package lv.dium.riskserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.Integer.valueOf;

public class Persistance {
    /**
     * Save game state to Redis
     * <p>
     * Saves game state to Redis database. Requires JedisConnection static class to obtain connection.
     * Also triggers save on areas and players. Only will actually do something if id is not null.
     * </p>
     *
     * @return current Game instance
     */
    public static void gameSave(Game game){
        Number id = game.getId();
        if(id == null){
            return;
        }

        String gameKey = "risk.game:" + id;

        try {
            JedisConnection.getLink().hset(gameKey, "scenario", game.getScenarioName());
            JedisConnection.getLink().hset(gameKey, "maxPlayers", game.getMaxPlayers().toString());
            JedisConnection.getLink().hset(gameKey, "phase", game.getCurrentPhase());
            JedisConnection.getLink().hset(gameKey, "player", game.getCurrentPlayer());
        }
        catch(Exception e){
            System.out.println("Failed to save game[" + id + "]");
        }

        // Save players
        try {
            JedisConnection.getLink().hset(gameKey, "numberOfPlayers", String.valueOf(game.getPlayers().size()));
            game.getPlayers().forEach((p) ->{
                String playerHashName = "GPlayer:"+game.getId()+":" + p.getId();
                System.out.println("Save [" + playerHashName + "]");

                JedisConnection.getLink().hset(playerHashName, "color", p.getColor());
                JedisConnection.getLink().hset(playerHashName, "id", p.getId());
                JedisConnection.getLink().hset(playerHashName, "name", p.getName());
            });
        }
        catch(Exception e){
            System.out.println("Failed to save players for game[" + id + "]");
        }

        // Save areas
        try {
        JedisConnection.getLink().hset(gameKey, "numberOfAreas", String.valueOf(game.getAreas().size()));

        game.getAreas().forEach((a) -> {
            String areaHashName = "GArea:"+game.getId()+":"+a.getId();

            JedisConnection.getLink().hset(areaHashName, "x", a.getX());
            JedisConnection.getLink().hset(areaHashName, "y", a.getY());
            JedisConnection.getLink().hset(areaHashName, "str", a.getStr());
            JedisConnection.getLink().hset(areaHashName, "id", a.getId());
            JedisConnection.getLink().hset(areaHashName, "color", a.getColor());
        });
        }
        catch(Exception e){
            System.out.println("Failed to save area for game[" + id + "]");
        }
    }

    /**
     * Load game state from Redis
     * <p>
     * Load game state from Redis database. Requires JedisConnection static class to obtain connection.
     * Also triggers load on areas and players. Only will actually do something if id is not null.
     * </p>
     *
     * @return current Game instance
     */
    public static void gameLoad(Game game) {
        Number id = game.getId();

        if(id == null){
            return;
        }

        String gameKey = "risk.game:" + id;

        try {
            game.setScenarioName(JedisConnection.getLink().hget(gameKey, "scenario"));

            try {
                /* Load areas */
                String gameAreasCnt = JedisConnection.getLink().hget(gameKey, "numberOfAreas");

                Number AreasSize = valueOf(gameAreasCnt);

                IntStream.range(0, AreasSize.intValue()).forEach(i -> {
                    GameArea nextArea = new GameArea(String.valueOf(i), game);

                    String areaHashName = "GArea:" + game.getId() + ":" + nextArea.getId();

                    nextArea.setX(JedisConnection.getLink().hget(areaHashName, "x"));
                    nextArea.setY(JedisConnection.getLink().hget(areaHashName, "y"));
                    nextArea.setStr(JedisConnection.getLink().hget(areaHashName, "str"));
                    nextArea.setColor(JedisConnection.getLink().hget(areaHashName, "color"));

                    String setName = "SAreaLinks:" + game.getScenarioName()+":" + nextArea.getId();

                    Set linksInRedis = JedisConnection.getLink().smembers(setName);
                    if(linksInRedis!=null && linksInRedis.size()>0) {
                        Iterator setIterator = linksInRedis.iterator();
                        while (setIterator.hasNext()) {
                            String nextVal = (String) setIterator.next();
                            nextArea.getLinks().add(Integer.valueOf(nextVal));
                        }
                    }

                    game.getAreas().add(nextArea);
                });
            } catch (Exception e) {
                System.out.println("Game areas load exception");
                System.out.println(e);
            }

            try {
                /* load players */
                String gamePlayersCnt = JedisConnection.getLink().hget(gameKey, "numberOfPlayers");
                Number PlayersSize = valueOf(gamePlayersCnt);

                IntStream.range(0, PlayersSize.intValue()).forEach(i -> {
                    GamePlayer nextPlayer = new GamePlayer(String.valueOf(i), game);

                    String playerHashName = "GPlayer:" + game.getId() + ":" + id;
                    System.out.println("Load [" + playerHashName + "]");

                    nextPlayer.setColor(JedisConnection.getLink().hget(playerHashName, "color"));

                    if(nextPlayer.getColor() == null){
                        nextPlayer.setColor(game.findColorByIndex(Integer.valueOf(nextPlayer.getId())));
                    }

                    nextPlayer.setName(JedisConnection.getLink().hget(playerHashName, "name"));
                    if(nextPlayer.getName() == null){
                        nextPlayer.setName("JohnDoe");
                    }

                    game.getPlayers().add(nextPlayer);
                });
            }
            catch (Exception e) {
                System.out.println("Game players load exception");
                System.out.println(e);
            }

            game.setMaxPlayers(valueOf(JedisConnection.getLink().hget(gameKey, "maxPlayers")));
            game.setCurrentPhase(JedisConnection.getLink().hget(gameKey, "phase"));
            game.setCurrentPlayer(JedisConnection.getLink().hget(gameKey, "player"));
            game.setLoaded(true);

        } catch (Exception e) {
            System.out.println("Game load exception");
            System.out.println(e);
            throw e;
        }
    }
}