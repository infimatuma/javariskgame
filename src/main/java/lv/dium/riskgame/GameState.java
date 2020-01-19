package lv.dium.riskgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lv.dium.riskserver.GamePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@JsonIgnoreProperties(value = { "lock" })

public class GameState {
    private Integer id;

    private Integer maxPlayers = 2;
    private String scenarioName;

    private String currentPlayer = ""; // color
    private Integer currentPlayerIndex = 0; // in-game-id
    private String currentPhase = "";

    private ArrayList<GameArea> areas = new ArrayList<>();
    private ArrayList<GamePlayer> players = new ArrayList<>();
    private Map<String, String> playerColor = new HashMap<>();

    private ArrayList<String> allColors;

    private Integer unallocated_units = 0;

    public ReentrantLock lock = new ReentrantLock();

    @JsonCreator
    public GameState(@JsonProperty("id") Integer id,
                     @JsonProperty("maxPlayers") Integer maxPlayers,
                     @JsonProperty("scenarioName") String scenarioName,
                     @JsonProperty("currentPlayer") String currentPlayer,
                     @JsonProperty("currentPlayerIndex") Integer currentPlayerIndex,
                     @JsonProperty("currentPhase") String currentPhase,
                     @JsonProperty("areas") ArrayList<GameArea> areas,
                     @JsonProperty("players") ArrayList<GamePlayer> players,
                     @JsonProperty("playerColor") Map<String, String> playerColor,
                     @JsonProperty("allColors") ArrayList<String> allColors,
                     @JsonProperty("unallocated_units") Integer unallocated_units
                     )
    {
        this.id = id;
        this.maxPlayers = maxPlayers;
        this.scenarioName = scenarioName;
        this.currentPlayer = currentPlayer;
        this.currentPlayerIndex = currentPlayerIndex;
        this.currentPhase = currentPhase;
        this.areas = areas;
        this.players = players;
        this.playerColor = playerColor;
        this.allColors = allColors;
        this.unallocated_units = unallocated_units;
    }

    public GameState() {
        // Set colors
        allColors = new ArrayList<>();
        allColors.add("red");
        allColors.add("green");
        allColors.add("grey");
        allColors.add("blue");
        allColors.add("yellow");
        allColors.add("purple");
    }

    /**currentPhase
     * Route random start
     */
    public void randomStart() {
        if(maxPlayers.equals(2)) {
            randomStartTwoPlayers();
        }

        currentPhase = "recruit";
        currentPlayerIndex = 0;
        currentPlayer = allColors.get(currentPlayerIndex);
        unallocated_units = 5;
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

            for (GameArea area : areas) {
                int playerIndex = new Random().nextInt(2);
                area.setColor(allColors.get(playerIndex));
                area.setStr(1);
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

    /** determine next player's in-game color
     * */
    public String provideFreeColor() {
        String nextColor = null;
        try{
            nextColor = allColors.get(players.size());
        }
        catch (Exception e){
            System.out.println("Can not provideFreeColor: " + e);
        }
        return nextColor;
    }

    /** determine next player's in-game id
     * may return null
     * */
    private Integer getNewPlayerId() {
        Integer newID = null;
        try {
            if (players != null) {
                newID = players.size();
            } else {
                newID = 0;
            }
        } catch (Exception e) {
            System.out.println("Can not getNewPlayerId: " + e);
        }
        return newID;
    }

    public String findColorByIndex(Integer index) {
        String nextColor;
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

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public String findCurrentPlayerColor(){
        return allColors.get(currentPlayerIndex);
    }

    public String findColorByUsername(String username){
        return playerColor.get(username);
    }

    public Integer countUnallocated_units() {
        return unallocated_units;
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

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public void addUser(String username) {
        try {
            GamePlayer newPlayer = new GamePlayer(username, getNewPlayerId(), provideFreeColor());

            players.add(newPlayer);
            playerColor.put(newPlayer.getName(), newPlayer.getColor());

        } catch (Exception e) {
            System.out.println("Can not add user to game: " + e);
        }
    }

    public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Integer countAreaStr(Integer k) {
        return areas.get(k).getStr();
    }

}
