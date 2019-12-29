package lv.dium.riskserver;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

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

    public Game() {
        initialize();
    }
    public Game(Number gameId) {
        id = gameId;
        initialize();
    }

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
    public void start(){
        if(id == null && scenarioName != ""){
            id = JedisConnection.getLink().incr("risk.gameIds");
            if(id != null) {
                initialize();

                currentPhase = "setup";
                currentPlayer = "waiting-for-players";

                Scenario baseScenario = new Scenario(scenarioName);
                try{
                    baseScenario.load();

                    ArrayList<GameScenarioArea> scenarioAreas = baseScenario.getAreas();
                    scenarioAreas.forEach(this::createAreaFromScenarioArea);

                    isLoaded = true;
                }
                catch(Exception e){
                    System.out.println("Failed to create game[" + id + "]");
                }

            }
        }
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

    private String getNewPlayerId(){
        String newID = null;
        try{
            if(players != null){
                newID = String.valueOf(players.size());
            }
            else{
                newID = "0";
            }
        }
        catch (Exception e){
            System.out.println("Can not getNewPlayerId: " + e);
        }
        return newID;
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

    public String asJson(){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(this);
        }
        catch (Exception e){
            System.out.println("Failed writeValueAsString for game");
        }

        return json;
    }

    public Action handleAction(String action, String payload){
        // create basic Action out of message
        Action resolution = new Action(action, payload);
        System.out.println("Action instant = " + action + " [ " + payload + "]");

        /* no actions accepted until game is already running
        We always expect !g greetings before any actions
        We will force re-login if no active game state exists
         */

        if(isLoaded) {
            if (id != null && action != null) {
                /* should have id set now */
                /* lock the game before we load it */
                if (lock()) {
                    try {
                        /* handle actionMessage received by player here */

                        /* Following construction should be used to issue changes to client
                         * All valid game effects should be documented in GameEffect class
                         */

                        System.out.println("Action now = " + action);
                        GameActionHandler actionHandler = new GameActionHandler(this, resolution);
                        actionHandler.process();


                        /*
                        GameEffect nextEffect = new GameEffect();
                        resolution.addGameEffect(nextEffect);
                        */

                        //applyEffects(resolution.getEffects());

                    } catch (Exception e) {
                        System.out.println("Failed to process game[" + id + "] action[" + action + "]");
                    }
                    unlock();
                }
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

    public void addUser(MpUser user) {
        try {
            GamePlayer newPlayer = new GamePlayer(user.getUsername(), getNewPlayerId(), this);
            newPlayer.pickColor();
            players.add(newPlayer);
        }
        catch (Exception e){
            System.out.println("Can not add user to game: " + e);
        }
    }

}
