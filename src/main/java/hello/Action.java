package hello;

public class Action {

    private String action;
    private Number area;

    public Action() {
    }

    public Action(String action, Number area) {
        this.action = action;
        this.area = area;
    }

    public String getAction() {
        return action;
    }
    public Number getArea() {
        return area;
    }

}