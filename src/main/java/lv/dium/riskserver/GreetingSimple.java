package lv.dium.riskserver;

public class GreetingSimple {
    private String name;
    private Integer playersNow;
    private Integer playersNeed;

    public GreetingSimple(String name, Integer playersNow, Integer playersNeed) {
        this.name = name;
        this.playersNow = playersNow;
        this.playersNeed = playersNeed;
    }

    public String getName() {
        return name;
    }

    public Integer getPlayersNow() {
        return playersNow;
    }

    public Integer getPlayersNeed() {
        return playersNeed;
    }
}
