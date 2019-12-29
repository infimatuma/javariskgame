package lv.dium.riskserver;

public interface LoginHandler {
    default MpUser login(String username, String password){
        MpUser user = new MpUser(username, password, true);
        return user;
    }
    default MpUser parsePayload(String payload){
        String[] data = payload.split(":", 2);
        return new MpUser(data[0], data[1]);
    }

    default MpUser getUserFromPayload(String payload){
        MpUser userDraft = parsePayload(payload);
        MpUser userReal = login(userDraft.getUsername(), userDraft.getPassword());
        return userReal;
    }
}
