package es.uji.ei1027.sps.model;

import java.time.LocalDate;

public class Negotiation {
    private int idNegotiation;
    private String status;
    private LocalDate negotiationDate;
    private int idRequest;
    private String dniAssistant;

    public Negotiation() {}

    public int getIdNegotiation() {
        return idNegotiation;
    }
    public void setIdNegotiation(int idNegotiation) {
        this.idNegotiation = idNegotiation;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getNegotiationDate() {
        return negotiationDate;
    }
    public void setNegotiationDate(LocalDate negotiationDate) {
        this.negotiationDate = negotiationDate;
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
        return "Negotiation{" +
                "idNegotiation=" + idNegotiation +
                ", status='" + status + '\'' +
                ", negotiationDate=" + negotiationDate +
                ", idRequest=" + idRequest +
                ", dniAssistant='" + dniAssistant + '\'' +
                '}';
    }
}
