package hello;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;

public class Game {
    public Jedis jedis;

    private Number id;
    private String scenarioName;

    private String currentPlayer;
    private String currentPhase;

    private ArrayList<GameArea> areas;
    private ArrayList<GamePlayer> players;

    /* main use as of now */
    public Game() {
        scenarioName = "basic";
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

    private void connect(){
        if(jedis == null){
            jedis = new Jedis("paris.cloudyhost.info", 6379);
            System.out.println("Autentification " + jedis.auth("Risk#777b&"));
            System.out.println("Server Ping: " + jedis.ping());
        }
    }

    /* makes sure we have a current game state */
    public void initialize(){
        connect();

        if(id == null) {
            create();
        }
    }

    public void load(){
        if(id == null) {
            /* game does not exist */
        }
        else {
            /* game already exists */
        }
    }

    public void create(){
        if(id == null){
            /* game does not exist */
        }
        else{
            /* game already exists */
        }
    }

    private void setIdByPLayer(String playerId){
        // get player game if any

        // if no game linked to player - check for any game with free slots

        // if no game exists - create new game

        // link player to game

        // set id
    }

    private void lock(){

    }

    private void unlock(){

    }


    public Action handleAction(ActionMessage message){
        // create basic Action out of message
        Action resolution = new Action(message.getPlayerId(), message.getAction(), message.getArea(), message.getTargetArea(), message .getUnits());

        if(id == null){
            setIdByPLayer(message.getPlayerId());
        }

        if(id != null) {
            /* should have id set now */
            /* lock the game before we load it */
            lock();

            /* handle actionMessage received by player here */

            /* ----------------------------------------------- */

            /* unlock game before returning result */
            unlock();
        }
        /* return complete Action (should be broadcasted in controller) */
        return resolution;
    }
}
