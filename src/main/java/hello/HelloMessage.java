package hello;

public class HelloMessage {

    private String name;

    public HelloMessage() {
    }

    public HelloMessage(String name) {
        this.name = name;
    }

    public String getName() {
        // We do not want this to ever be null
        if(name == null){
            name = "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}