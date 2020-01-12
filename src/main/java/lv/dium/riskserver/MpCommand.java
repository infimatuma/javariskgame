package lv.dium.riskserver;

public class MpCommand {
    private final String value;
    private final String sendTo; // all, self, others

    public MpCommand(String value, String sendTo){
        this.value = value;
        this.sendTo = sendTo;
    }

    public String getValue() {
        return value;
    }

    public String getSendTo() {
        return sendTo;
    }

}
