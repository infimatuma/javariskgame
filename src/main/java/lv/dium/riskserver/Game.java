package lv.dium.riskserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static java.lang.Integer.*;

public class Game {
    private Number id; // Does it really have to be Number?

    private Boolean isLoaded = false;

    private String scenarioName = "small";
    private Integer maxPlayers = 2;

    private String currentPlayer = "";
    private String currentPhase = "";

    private ArrayList<GameArea> areas = new ArrayList<GameArea>();
    private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();

    private ArrayList<String> allColors = new ArrayList<String>();

    private GameAreasManager areasManager;

    private volatile Boolean isLocked = false;

    /**
     * Handles game initialization
     * <p>
     * Any mandatory actions to ensure game is valid should be taken here
     * </p>
     *
     * @return current Game instance
     */
    private Game initialize(){
        allColors = new ArrayList<>();
        allColors.add("red");
        allColors.add("green");
        allColors.add("grey");
        allColors.add("blue");
        allColors.add("yellow");
        allColors.add("purple");

        return this;
    }

    /**
     * Save game state to Persistance
     * @return current Game instance
     */
    public Game save(){
        /* we can only save game if id is set */
        if(id != null) {
            Persistance.gameSave(this);
        }
        return this;
    }

    /**
     * Load game state from Persistance
     * @return current Game instance
     */
    public Game load() throws Exception{
        if(id != null) {
            Persistance.gameLoad(this);
        }
        return this;
    }

    /* created a new game for provided payer using game set scenario (default scenario exists) */
    public Game create(String playerNumberOneId){
        if(id == null && scenarioName != ""){
            id = JedisConnection.getLink().incr("risk.gameIds");
            if(id != null && lock()) {
                currentPhase = "setup";
                currentPlayer = "waiting-for-players";

                // Make sure those are empty
                areas = new ArrayList<GameArea>();
                players = new ArrayList<GamePlayer>();

                Scenario baseScenario = new Scenario(scenarioName);
                try{
                    baseScenario.load();

                    ArrayList<GameScenarioArea> scenarioAreas = baseScenario.getAreas();
                    scenarioAreas.forEach(this::createAreaFromScenarioArea);

                    if (playerNumberOneId != null) {
                        GamePlayer playerNumberOne = new GamePlayer(playerNumberOneId, "0", this);
                        players.add(playerNumberOne.pickColor());
                    }

                    // Save initial game state
                    save();
                }
                catch(Exception e){
                    System.out.println("Failed to create game[" + id + "]");
                }
                unlock();

                try{
                    GamePool.addGameToPool(id); /* lock is not mandatory as it is ok to have multiply games in pool */
                }
                catch (Exception e){
                    System.out.println("Failed to add game to pool[" + id + "]");
                }
            }
        }
        return this;
    }

    /* Copy all required properties from GameScenarioArea into new GameArea */
    private void createAreaFromScenarioArea(GameScenarioArea area) {
        if(area != null){
            GameArea newArea = new GameArea(area, this);
            areas.add(newArea);
        }
    }

    public String getFreeColor(){
        return allColors.get(players.size());
    }

    private Game setIdByPLayer(String playerId) {
        // get player game if any
        String stringId = null;
        try {
            stringId = JedisConnection.getLink().get("player:" + playerId);
        }
        catch (Exception e){
            System.out.println("Redis error while getting players[" + playerId + "] current game game");
        }

        if(stringId != null) {
            id = valueOf(stringId);
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
                try {
                    JedisConnection.getLink().set("player:" + playerId, String.valueOf(id));
                }
                catch (Exception e){
                    System.out.println("Redis error while setting players[" + playerId + "] current game game");
                }
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

    /** Try to find a game with free slot and make it active game if one exists
     * @param playerId login name of a player we should look for a game
     **/
    private void findSlotForPlayer(String playerId) {
        System.out.println("Will look for free slot for player [" + playerId + "]");

        // Lock game pool to avoid bad things
        if(GamePool.lock()){
            Number greatGameId = GamePool.getGameId();
            if(greatGameId != null){
                id = greatGameId;
                System.out.println("We picked game " + id);
                if(lock()){
                    try {
                        // Load everything-game
                        load();

                        // Create player object and add it to game
                        GamePlayer nextPlayer = new GamePlayer(playerId, getNewPlayerId(), this);
                        players.add(nextPlayer.pickColor());

                        // Pass current state to GamePool
                        GamePool.fixGameStateInPool(id, maxPlayers, players.size());
                    }
                    catch (Exception e) {
                        System.out.println("Failed to load game[" + greatGameId + "] and assign new player[" + playerId + "]");
                        unlock();
                    }
                }
            }
        }
        GamePool.unlock();
    }

    private boolean lock(){
        /* redis.setnx ?*/

        try {
            Integer totalWaitTime = 0;
            while(isLocked){
                Thread.sleep(100);
                totalWaitTime += 100;

                if(totalWaitTime > 5000){
                    throw new Exception("Failed to acquire game lock (5s).");
                }
            }
            isLocked = true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    private void unlock(){
        /* redis.setnx ? */
        isLocked = false;
    }

    private Game applyEffects(ArrayList<GameEffect> effects){
        /* not sure if this is needed */
        return this;
    }

    public String greetPlayer(String PlayerStringId){
        // create basic Action out of message
        Greeting resolution = new Greeting("Hello, " + HtmlUtils.htmlEscape(PlayerStringId) + "!");

        // Check if we have a PlayerId and find valid game
        if(PlayerStringId != ""){
            try{
                initialize().setIdByPLayer(PlayerStringId);
            }
            catch (Exception e){
                System.out.println("Failed to get game by playerId [" + PlayerStringId + "]");
            }
        }

        if(id != null) {
            // lock the game before we load it
            if(lock()){
                try{
                    // Load current game and pass link to resolution
                    load();
                    resolution.setGame(this);
                }
                catch (Exception e){
                    System.out.println("Failed to handle Greeting from player[" + PlayerStringId + "]");
                }
                unlock();
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(resolution);
        }
        catch (Exception e){
            System.out.println("Failed writeValueAsString for resolution");
        }
        return json;
    }
    public Greeting handleGreeting(HelloMessage message){
        // Extract PlayerId from message
        String PlayerStringId = message.getName();
        if(PlayerStringId == null){
            PlayerStringId = "";
        }

        // create basic Action out of message
        Greeting resolution = new Greeting("Hello, " + HtmlUtils.htmlEscape(PlayerStringId) + "!");

        // Check if we have a PlayerId and find valid game
        if(PlayerStringId != ""){
            try{
                initialize().setIdByPLayer(PlayerStringId);
            }
            catch (Exception e){
                System.out.println("Failed to get game by playerId [" + PlayerStringId + "]");
            }
        }

        if(id != null) {
            // lock the game before we load it
            if(lock()){
                try{
                    // Load current game and pass link to resolution
                    load();
                    resolution.setGame(this);
                }
                catch (Exception e){
                    System.out.println("Failed to handle Greeting from player[" + message.getName() + "]");
                }
                unlock();
            }
        }

        return resolution;
    }

    public Action handleAction(ActionMessage message){
        // create basic Action out of message
        Action resolution = new Action(message.getPlayerId(), message.getAction(), message.getArea(), message.getTargetArea(), message.getUnits());
        System.out.println("Action instant = " + resolution.getAction() + ", "+message.getAction());

        try {
            initialize().setIdByPLayer(message.getPlayerId());
        }
        catch (Exception e){
            System.out.println("Failed to get game by playerId [" + message.getPlayerId() + "]");
        }

        String receivedAction = resolution.getAction();

        if(id != null && receivedAction != null) {
            /* should have id set now */
            /* lock the game before we load it */
            if(lock()){
                try {
                    load(); // Load current game. Even if it is loaded already - we have to be sure we nobody changed anything in between.

                    /* handle actionMessage received by player here */

                    /* Following construction should be used to issue changes to client
                     * All valid game effects should be documented in GameEffect class
                     */

                    System.out.println("Action now = " + receivedAction);
                    GameActionHandler actionHandler = new GameActionHandler(this, resolution);
                    actionHandler.process();


                    /*
                    GameEffect nextEffect = new GameEffect();
                    resolution.addGameEffect(nextEffect);
                    */

                    //applyEffects(resolution.getEffects());

                    /* ----------------------------------------------- */

                    /* save & unlock game before returning result */
                    save();
                }
                catch (Exception e){
                    System.out.println("Failed to process game[" + id + "] action[" + receivedAction + "]");
                }
                unlock();
            }
        }
        /* return complete Action (should be broadcasted in controller) */
        return resolution;
    }

    public String findColorByIndex(Integer index){
        String nextColor = null;
        if(allColors.size() > index){
            nextColor = allColors.get(index);
        }
        else{
            nextColor = "";
        }
        return nextColor;
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

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public GameAreasManager findAreasManager() {
        if(areasManager == null){
            areasManager = new GameAreasManager(this);
        }
        return areasManager;
    }

    public void setLoaded(Boolean loaded) {
        isLoaded = loaded;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }
}
