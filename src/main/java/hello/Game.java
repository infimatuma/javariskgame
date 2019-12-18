package hello;

import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
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
        allColors = new ArrayList<>();
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

            String gameKey = "risk.game:" + id;

            try {
                areas = new ArrayList<>();
                players = new ArrayList<>();

                scenarioName = JedisConnection.getLink().hget(gameKey, "scenario");
                maxPlayers = Integer.valueOf(JedisConnection.getLink().hget(gameKey, "maxPlayers"));
                currentPhase = JedisConnection.getLink().hget(gameKey, "phase");
                currentPlayer = JedisConnection.getLink().hget(gameKey, "player");

                loadAreas();
                loadPlayers();

                isLoaded = true;

            } catch (Exception e) {
                System.out.println("Game load exception");
                System.out.println(e);
            }

        }
        return this;
    }

    public void loadAreas(){
        String gameKey = "risk.game:" + id;

        try {
            /* Load areas */
            String gameAreasCnt = JedisConnection.getLink().hget(gameKey, "numberOfAreas");

            Number AreasSize = Integer.valueOf(gameAreasCnt);

            IntStream.range(0, AreasSize.intValue()).forEach(i -> {
                GameArea nextArea = new GameArea(String.valueOf(i), this);
                areas.add(nextArea.load());
            });
        } catch (Exception e) {
            System.out.println("Game areas load exception");
            System.out.println(e);
        }
    }

    public void loadPlayers(){
        String gameKey = "risk.game:" + id;

        try {
            /* load players */
            String gamePlayersCnt = JedisConnection.getLink().hget(gameKey, "numberOfPlayers");
            Number PlayersSize = Integer.valueOf(gamePlayersCnt);

            IntStream.range(0, PlayersSize.intValue()).forEach(i -> {
                GamePlayer nextPlayer = new GamePlayer(String.valueOf(i), this);
                players.add(nextPlayer.load());
            });
        }
        catch (Exception e) {
            System.out.println("Game players load exception");
            System.out.println(e);
        }
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
                    GamePlayer playerNumberOne = new GamePlayer(playerNumberOneId, "0", this);
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

    private Game setIdByPLayer(String playerId) {
        // get player game if any

        String stringId = JedisConnection.getLink().get("player:" + playerId);
        if(stringId != null) {
            id = Integer.valueOf(stringId);
        }

        if(id != null){
            System.out.println("Got game {" + id + "} by player id " + playerId);
        }

        if (id == null) {
            // if no game linked to player - check for any game with free slots
            findSlotForPlayer(playerId);

            // if still no game - create new game
            if (id == null) {
                System.out.println("Will create new game");
                create(playerId);
            }
            // link player to game
            if (id != null) {
                System.out.println("Setting game {" + id + "} for player player:" + playerId);
                JedisConnection.getLink().set("player:" + playerId, String.valueOf(id));
            }
        }
        return this;
    }

    private String getNewPlayerId(){
        String newID;
        if(players != null){
            newID = String.valueOf(players.size());
        }
        else{
            newID = "0";
        }
        return newID;
    }
    /* Try to find a game with free slot and make it active game if one exists */
    private void findSlotForPlayer(String playerId) {
        /* Lock game pool to avoid bad things */
        System.out.println("Will look for free slot for player [" + playerId + "]");
        if(GamePool.lock()){
            Number greatGameId = GamePool.getGameId();
            if(greatGameId != null){
                id = greatGameId;
                System.out.println("We picked game " + id);
                if(lock()){
                    load(); /* load everything-game */
                    GamePlayer nextPlayer = new GamePlayer(playerId, getNewPlayerId(), this);
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

    private Game applyEffects(ArrayList<GameEffect> effects){
        /* not sure if this is needed */
        return this;
    }

    public Greeting handleGreeting(HelloMessage message){
        // create basic Action out of message
        Greeting resolution = new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");

        initialize().setIdByPLayer(message.getName());

        if(id != null) {
            /* should have id set now */
            /* lock the game before we load it */
            if(lock()){
                load(); // Load current game.
                resolution.setGame(this);
                unlock();
            }
        }
        /* return complete Greeting (should be broadcasted in controller) - everybody wll have fresh game state */
        return resolution;
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

    public String findColorByIndex(Integer index){
        return allColors.get(index);
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

    public ArrayList<GameArea> getAreas() {
        return areas;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public ArrayList<GamePlayer> getPlayers() {
        return players;
    }
}
