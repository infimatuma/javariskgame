package hello;

public class ActionMessage {

    private String action;
    private Number area;

    public ActionMessage(String action, Number area) {
        this.action = action;
        this.area = area;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Number getArea() {
        return area;
    }

    public void setArea(Number area) {
        this.area = area;
    }
}