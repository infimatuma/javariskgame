package lv.dium.riskserver;

public class MpUser {
    private String username;
    private String password;
    private String id;
    private boolean isAuthorized;

    public MpUser() {
        this(null, null, false);
    }
    public MpUser(String username, String password) {
        this(username, password, false);
    }

    public MpUser(String username, String password, boolean isAuthorized) {
        this.username = username.toLowerCase();
        this.password = password.toLowerCase();
        this.isAuthorized = isAuthorized;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}
