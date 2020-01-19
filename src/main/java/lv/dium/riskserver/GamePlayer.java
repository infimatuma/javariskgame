package lv.dium.riskserver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GamePlayer {
    final String name;
    final String color;
    final Integer id;

    @JsonCreator
    public GamePlayer(@JsonProperty("name") String name, @JsonProperty("id") Integer id, @JsonProperty("color") String color){
        this.color = color;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Integer getId() {
        return id;
    }

}
