package lv.dium.riskserver;

public class GamePool {
    private static String poolHash = "risk.free.games";
    public static Number getGameId(){
        Number nextId = null;

        Number totalGamesInPool = JedisConnection.getLink().llen(poolHash);
        if(totalGamesInPool.intValue() > 0){
            // get oldest game
            String oldestGameId = JedisConnection.getLink().lindex(poolHash, 0);
            nextId = Integer.valueOf(oldestGameId);
        }

        return nextId;
    }

    public static void fixGameStateInPool(Number gameId, Integer maxPlayers, Integer numberOfPlayers){
        if(numberOfPlayers >= maxPlayers){
            JedisConnection.getLink().lrem(poolHash, 1, gameId.toString());
        }
    }

    public static void addGameToPool(Number gameId){
        JedisConnection.getLink().rpush(poolHash, gameId.toString());
    }

    public static boolean lock() {
        return true;
    }

    public static void unlock() {
    }
}
