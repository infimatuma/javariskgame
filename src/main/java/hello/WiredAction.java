package hello;

/** A description of valid ActionProcessor
 */

public class WiredAction {
    private String actionName;
    private String className;

    public WiredAction(String actionName, String className) {
        this.actionName = actionName;
        this.className = className;
    }

    public String getActionName() {
        return actionName;
    }

    public String getClassName() {
        return className;
    }
}
