package hello;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

public class Game {
    private Number id;

    private Boolean isLoaded = false;

    private String scenarioName;
    private Number maxPlayers;

    private String currentPlayer;
    private String currentPhase;

    private ArrayList<GameArea> areas;
    private ArrayList<GamePlayer> players;

    private ArrayList<String> allColors;

    /* main use as of now */
    public Game() {
        scenarioName = "small";
        maxPlayers = 2;
    }

    /* Create game by id */
    public Game(Number id) {
        this.id = id;
    }

    /* create game and set scenario */
    public Game(Number id, String scenarioName) {
        this.id = id;
        this.scenarioName = scenarioName;
    }

    /* makes sure we have a current game state */
    public Game initialize(){
        allColors.add("red");
        allColors.add("green");
        allColors.add("grey");
        allColors.add("blue");
        allColors.add("yellow");

        /*
        if(id == null) {
            create();
        }*/
        return this;
    }

    public Game save(){
        /* we can only save game if id is set */
        if(id != null) {
            String gameKey = "risk.game:"+id;

            JedisConnection.getLink().hset(gameKey, "scenario", scenarioName);
            JedisConnection.getLink().hset(gameKey, "maxPlayers", maxPlayers.toString());
            JedisConnection.getLink().hset(gameKey, "phase", currentPhase);
            JedisConnection.getLink().hset(gameKey, "player", currentPlayer);

            saveAreas();
            savePlayers();
        }
        return this;
    }
    public Game savePlayers(){
        /* we can only save game if id is set */
        if(id != null) {
            String gameKey = "risk.game:"+id;
            JedisConnection.getLink().hset(gameKey, "numberOfPlayers", String.valueOf(players.size()));
            players.forEach(GamePlayer::save);
        }
        return this;
    }

    public Game saveAreas(){
        /* we can only save game if id is set */
        if(id != null) {
            String gameKey = "risk.game:"+id;
            JedisConnection.getLink().hset(gameKey, "numberOfAreas", String.valueOf(areas.size()));
            areas.forEach(GameArea::save);
        }
        return this;
    }


    public Game load(){
        if(id != null) {
            System.out.println("wanna load game [" + id + "]!");

            areas = new ArrayList<>();

            try {

                String gameKey = "risk.scenario:" + id;
                String gameAreasCnt = JedisConnection.getLink().hget(gameKey, "numberOfAreas");

                Number AreasSize = Integer.valueOf(gameAreasCnt);

                IntStream.range(0, AreasSize.intValue()).forEach(i -> {
                    GameArea nextArea = new GameArea(String.valueOf(i), this);
                    areas.add(nextArea.load());
                });

                isLoaded = true;

            } catch (Exception e) {
                System.out.println(e);
            }

        }
        return this;
    }

    /* created a new game for provided payer using game set scenario (default scenario exists) */
    public Game create(String playerNumberOneId){
        if(id == null){
            id = JedisConnection.getLink().incr("risk.gameIds");
            if(lock()) {
                currentPhase = "setup";
                currentPlayer = "waiting-for-players";

                areas = new ArrayList<GameArea>();
                players = new ArrayList<GamePlayer>();

                Scenario baseScenario = new Scenario(scenarioName);
                baseScenario.load();

                ArrayList<GameScenarioArea> scenarioAreas = baseScenario.getAreas();
                scenarioAreas.forEach(this::createAreaFromScenarioArea);

                if (playerNumberOneId != null) {
                    GamePlayer playerNumberOne = new GamePlayer(playerNumberOneId, this);
                    players.add(playerNumberOne.pickColor());
                }

                save(); /* save initial game state */
                unlock();

                GamePool.addGameToPool(id); /* lock is not mandatory as it is ok to have multiply games in pool */
            }
        }
        return this;
    }

    /* Copy all required properties from GameScenarioArea into new GameArea */
    private void createAreaFromScenarioArea(GameScenarioArea area) {
        GameArea newArea = new GameArea(area, this);
        areas.add(newArea);
    }

    public String getFreeColor(){
        return allColors.get(players.size());
    }

    private Game applyEffects(ArrayList<GameEffect> effects){
        /* not sure if this is needed */
        return this;
    }

    private Game setIdByPLayer(String playerId) {
        // get player game if any

        String id = JedisConnection.getLink().get("player:" + playerId);

        if (id == null) {
            // if no game linked to player - check for any game with free slots
            findSlotForPlayer(playerId);

            // if still no game - create new game
            if (id == null) {
                create(playerId);
            }
            // link player to game
            if (id != null) {
                JedisConnection.getLink().set("player:" + playerId, id);
            }
        }
        return this;
    }

    /* Try to find a game with free slot and make it active game if one exists */
    private void findSlotForPlayer(String playerId) {
        /* Lock game pool to avoid bad things */
        if(GamePool.lock()){
            Number greatGameId = GamePool.getGameId();
            if(greatGameId != null){
                id = greatGameId;
                if(lock()){
                    load(); /* load everything-game */
                    GamePlayer nextPlayer = new GamePlayer(playerId, this);
                    players.add(nextPlayer.pickColor());

                    savePlayers(); /* save game players while pool is locked */
                    GamePool.fixGameStateInPool(id, maxPlayers.intValue(), players.size());

                    unlock();
                }
            }
        }
        GamePool.unlock();
    }

    private boolean lock(){
        /* redis.setnx */
        return true;
    }

    private void unlock(){
        /* redis.setnx */
    }


    public Action handleAction(ActionMessage message){
        // create basic Action out of message
        Action resolution = new Action(message.getPlayerId(), message.getAction(), message.getArea(), message.getTargetArea(), message.getUnits());

        initialize().setIdByPLayer(message.getPlayerId());

        if(id != null) {
            /* should have id set now */
            /* lock the game before we load it */
            if(lock()){
                load(); // Load current game. Even if it is loaded already - we have to be sure we nobody changed anything in between.

                /* handle actionMessage received by player here */

                /* Following construction should be used to issue changes to client
                * All valid game effects should be documented in GameEffect class
                */

                /*
                GameEffect nextEffect = new GameEffect();
                resolution.addGameEffect(nextEffect);
                */

                applyEffects(resolution.getEffects());

                /* ----------------------------------------------- */

                /* save & unlock game before returning result */
                save().unlock();
            }
        }
        /* return complete Action (should be broadcasted in controller) */
        return resolution;
    }

    public Number getId() {
        return id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }
}
