package lv.dium.riskserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Game {
    private Number id; // Does it really have to be Number?

    private Boolean isLoaded = false;

    private String scenarioName = "basic";
    private Integer maxPlayers = 2;

    private String currentPlayer = "";
    private Integer currentPlayerIndex = 0;
    private String currentPhase = "";

    private ArrayList<GameArea> areas = new ArrayList<GameArea>();
    private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();
    private Map<String, String> playerColor = new HashMap<>();

    private ArrayList<String> allColors = new ArrayList<String>();

    private GameAreasManager areasManager;

    private volatile Boolean isLocked = false;
    private DefaultChannelGroup channelGroup;

    private Number unallocated_units = 0;

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
    private Game initialize() {
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
     *
     * @return current Game instance
     */
    public Game save() {
        /* we can only save game if id is set */
        if (id != null) {
            Persistance.gameSave(this);
        }
        return this;
    }

    /**
     * Load game state from Persistance
     *
     * @return current Game instance
     */
    public Game load() throws Exception {
        if (id != null) {
            Persistance.gameLoad(this);
        }
        return this;
    }

    /* created a new game for provided payer using game set scenario (default scenario exists) */
    public void start() {
        if (id == null && scenarioName != "") {
            id = JedisConnection.getLink().incr("risk.gameIds");
            if (id != null) {
                initialize();

                currentPhase = "setup";
                currentPlayer = "waiting-for-players";

                Scenario baseScenario = new Scenario(scenarioName);
                try {
                    baseScenario.load();

                    ArrayList<GameScenarioArea> scenarioAreas = baseScenario.getAreas();
                    scenarioAreas.forEach(this::createAreaFromScenarioArea);

                    randomStart();

                    currentPhase = "recruit";
                    currentPlayerIndex = 0;
                    currentPlayer = allColors.get(currentPlayerIndex);
                    unallocated_units = 5;

                    isLoaded = true;
                } catch (Exception e) {
                    System.out.println("Failed to create game[" + id + "]");
                }
            }
        }
    }

    /**currentPhase
     * Route random start
     */
    private void randomStart() {
        if(maxPlayers.equals(2)) {
            randomStartTwoPlayers();
        }
    }

    /**
     * Process random start fro 2 players game
     */
    private void randomStartTwoPlayers() {
        int playersCnt = 2;
        int unitsPerPlayer = 40;
        int territoryCnt = areas.size();
        int[] remainingUnitsPerPlayer = new int[playersCnt];

        System.out.println("Will randomize 2 player game");

        try {
            for (int i = 0; i < playersCnt; i++) {
                remainingUnitsPerPlayer[i] = unitsPerPlayer;
            }

            for (int i = 0; i < territoryCnt; i++) {
                int playerIndex = new Random().nextInt(2);
                areas.get(i).setColor(allColors.get(playerIndex));
                areas.get(i).setStr(1);
                remainingUnitsPerPlayer[playerIndex]--;
            }

            for (int playerIndex = 0; playerIndex < playersCnt; playerIndex++) {
                int tIndex = 0;
                while (remainingUnitsPerPlayer[playerIndex] > 0) {
                    GameArea currentArea = areas.get(tIndex);

                    if (allColors.get(playerIndex).equals(currentArea.getColor())) {
                        int addedValue = new Random().nextInt(4);
                        if (remainingUnitsPerPlayer[playerIndex] < addedValue) {
                            addedValue = remainingUnitsPerPlayer[playerIndex];
                        }
                        currentArea.addStr(addedValue);
                        remainingUnitsPerPlayer[playerIndex] = remainingUnitsPerPlayer[playerIndex] - addedValue;
                    }

                    tIndex++;
                    if (tIndex >= territoryCnt) {
                        tIndex = 0;
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("Randomizing 2 players game failed." + e);
        }
    }
    /* check if switching phase is valid */
    public boolean checkIfValidSwitchPhase() {
        boolean isOK = true;

        if(currentPhase.equals("recruit")){
            // must allocate all units before continue
            if(unallocated_units.intValue() > 0){
                isOK = false;
            }
        }

        return isOK;
    }

    /**  Switch phase to next
     * ONLY performs switch, no validation done here
     * */
    public void goNextPhase(){
        if(currentPhase.equals("setup")){
            if(maxPlayers > currentPlayerIndex + 1) {
                currentPlayerIndex++;
            }
            else{
                currentPhase = "recruit";
                currentPlayerIndex = 0;
            }
        }
        if(currentPhase.equals("recruit")){
            currentPhase = "attack";
        }
        else if(currentPhase.equals("attack")){
            currentPhase = "reinforce";
        }
        else if(currentPhase.equals("reinforce")){
            if(maxPlayers > currentPlayerIndex + 1) {
                currentPlayerIndex++;
                currentPhase = "recruit";
            }
            else{
                currentPlayerIndex = 0;
                currentPhase = "recruit";
            }
        }
    }

/* Copy all required properties from GameScenarioArea into new GameArea */
    private void createAreaFromScenarioArea(GameScenarioArea area) {
        if (area != null) {
            GameArea newArea = new GameArea(area, this);
            areas.add(newArea);
        }
    }

    public String provideFreeColor() {
        return allColors.get(players.size());
    }

    private String getNewPlayerId() {
        String newID = null;
        try {
            if (players != null) {
                newID = String.valueOf(players.size());
            } else {
                newID = "0";
            }
        } catch (Exception e) {
            System.out.println("Can not getNewPlayerId: " + e);
        }
        return newID;
    }


    private synchronized boolean lock() {
        try {
            Integer totalWaitTime = 0;
            while (isLocked) {
                Thread.sleep(100);
                totalWaitTime += 100;

                if (totalWaitTime > 5000) {
                    throw new Exception("Failed to acquire game lock (5s).");
                }
            }
            isLocked = true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return true;
    }

    private void unlock() {
        /* redis.setnx ? */
        isLocked = false;
    }

    private Game applyEffects(ArrayList<GameEffect> effects) {
        /* not sure if this is needed */
        return this;
    }

    public String asJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            System.out.println("Failed writeValueAsString for game");
        }

        return json;
    }

    public Action handleAction(MpUser user, String action, String payload) {
        // create basic Action out of message
        Action resolution = new Action(action, payload);
        System.out.println("Action instant = " + action + " [ " + payload + "]");

        /* no actions accepted until game is already running
        We always expect !g greetings before any actions
        We will force re-login if no active game state exists
         */

        if (isLoaded) {
            if (id != null && action != null) {
                /* should have id set now */
                /* lock the game before we load it */
                if (lock()) {
                    try {
                        /* handle actionMessage received by player here */

                        /* Following construction should be used to issue changes to client
                         * All valid game effects should be documented in GameEffect class
                         */
                        // No username-s should be used inside game - every area belongs to one of colors for better abstraction and less validation
                        String actingColor = playerColor.get(user.getUsername());
                        resolution.setActingColor(actingColor);

                        if(validateAction(resolution)) {
                            GameActionHandler actionHandler = new GameActionHandler(this, resolution);
                            actionHandler.process();
                        }

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

    /** validate if provided action is valid at this point and should be processed */
    private boolean validateAction(Action resolution) {
        boolean isOk = true;

        if(!resolution.getActingColor().equals(allColors.get(currentPlayerIndex))){
            System.out.println("Wrong action player: " + resolution.getActingColor() + " != " + allColors.get(currentPlayerIndex));
            isOk = false;
        }
        else if(resolution.getAction().equals("a")){
            // validate attack permissions
            if(!currentPhase.equals("attack")){
                System.out.println("Wrong currentPhase (need attack): " + currentPhase);
                isOk = false;
            }
        }
        else if(resolution.getAction().equals("r")){
            // validate recruit permissions
            if(!currentPhase.equals("recruit")){
                System.out.println("Wrong currentPhase (need recruit): " + currentPhase);
                isOk = false;
            }
        }

        System.out.println("Action validation: " + String.valueOf(isOk));

        return isOk;
    }

    public String findColorByIndex(Integer index) {
        String nextColor = null;
        if (allColors.size() > index) {
            nextColor = allColors.get(index);
        } else {
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

    public Number getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Number countUnallocated_units() {
        return unallocated_units;
    }

    public GameAreasManager findAreasManager() {
        if (areasManager == null) {
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
            playerColor.put(newPlayer.getName(), newPlayer.getColor());

        } catch (Exception e) {
            System.out.println("Can not add user to game: " + e);
        }
    }

    public void setChannelGroup(DefaultChannelGroup gameChannelGroup) {
        this.channelGroup = gameChannelGroup;
    }

    public DefaultChannelGroup broadcastList() {
        return this.channelGroup;
    }

    public void replaceOrSetChannel(Channel oldChannel, Channel newChannel) {

        System.out.println("Need to replace channel " + oldChannel.id() + " with " + newChannel.id());

        if (oldChannel == null) return;
        if (newChannel == null) return;

        if (lock()) {
            System.out.println("Got game lock");
            try {
                for (Channel c : broadcastList()) {
                    if (c.id() == oldChannel.id()) {
                        broadcastList().remove(c);
                        System.out.println("Removed one channel");
                    }
                }
                broadcastList().add(newChannel);
                System.out.println("Added one channel");
            } catch (Exception e) {
                System.out.println("Failed replaceOrSetChannel " + e);
            }
            unlock();
            System.out.println("Released game lock");
        }

    }
}
