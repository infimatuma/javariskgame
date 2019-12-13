package hello;

public class Greeting {

    private String content;
    private GameFullState gameState;

    public Greeting() {
    }

    public Greeting(String content, GameFullState gameState) {
        this.content = content;
        this.gameState = gameState;
    }

    public String getContent() {
        return content;
    }

    public GameFullState getGameState() {
        return gameState;
    }

    public void setGameState(GameFullState gameState) {
        this.gameState = gameState;
    }
}