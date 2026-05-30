package es.uji.ei1027.sps.model;

public class SupportChat {
    private int id;
    private String participantDni;
    private String participantType;
    private String participantName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParticipantDni() {
        return participantDni;
    }

    public void setParticipantDni(String participantDni) {
        this.participantDni = participantDni;
    }

    public String getParticipantType() {
        return participantType;
    }

    public void setParticipantType(String participantType) {
        this.participantType = participantType;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
}
