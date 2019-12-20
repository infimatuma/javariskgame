package hello;

import java.util.ArrayList;

public class RiskActionProcessors {
    public static ArrayList<WiredAction> wiredActions = new ArrayList<WiredAction>();
    public static void addAction(String actionName, String className){
        WiredAction wiredAction = new WiredAction(actionName, className);
        wiredActions.add(wiredAction);
    }
    public static ArrayList<WiredAction> getWiredActions(){
        return wiredActions;
    }
}
