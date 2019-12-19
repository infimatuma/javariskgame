package hello;

public class GreetingSimple {
    private String name;
    private Number playersNow;
    private Number playersNeed;

    public GreetingSimple(String name, Number playersNow, Number playersNeed) {
        this.name = name;
        this.playersNow = playersNow;
        this.playersNeed = playersNeed;
    }

    public String getName() {
        return name;
    }

    public Number getPlayersNow() {
        return playersNow;
    }

    public Number getPlayersNeed() {
        return playersNeed;
    }
}
