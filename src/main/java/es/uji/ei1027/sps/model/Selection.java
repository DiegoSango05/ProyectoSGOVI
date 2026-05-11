package es.uji.ei1027.sps.model;

public class Selection {
    private int idRequest;
    private String dniAssistant;

    public Selection() {
    }

    public int getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(int idRequest) {
        this.idRequest = idRequest;
    }

    public String getDniAssistant() {
        return dniAssistant;
    }

    public void setDniAssistant(String dniAssistant) {
        this.dniAssistant = dniAssistant;
    }

    @Override
    public String toString() {
        return "Selection{" +
                "idRequest=" + idRequest +
                ", dniAssistant='" + dniAssistant + '\'' +
                '}';
    }
}
