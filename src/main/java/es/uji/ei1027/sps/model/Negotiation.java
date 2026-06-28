package es.uji.ei1027.sps.model;

import java.time.LocalDate;

public class Negotiation {
    private int idNegotiation;
    private String status;
    private LocalDate negotiationDate;
    private int idRequest;
    private String dniAssistant;
    private boolean acceptedCustomer;
    private boolean acceptedAssistant;
    private String dniOVIUser;
    private String nameOVIUser;
    private String nameAssistant;

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

    public boolean isAcceptedCustomer() {
        return acceptedCustomer;
    }
    public void setAcceptedCustomer(boolean acceptedCustomer) {
        this.acceptedCustomer = acceptedCustomer;
    }

    public boolean isAcceptedAssistant() {
        return acceptedAssistant;
    }
    public void setAcceptedAssistant(boolean acceptedAssistant) {
        this.acceptedAssistant = acceptedAssistant;
    }

    public String getDniOVIUser() {
        return dniOVIUser;
    }
    public void setDniOVIUser(String dniOVIUser) {
        this.dniOVIUser = dniOVIUser;
    }

    public String getNameOVIUser() {
        return nameOVIUser;
    }
    public void setNameOVIUser(String nameOVIUser) {
        this.nameOVIUser = nameOVIUser;
    }

    public String getNameAssistant() {
        return nameAssistant;
    }
    public void setNameAssistant(String nameAssistant) {
        this.nameAssistant = nameAssistant;
    }

    @Override
    public String toString() {
        return "Negotiation{" +
                "idNegotiation=" + idNegotiation +
                ", status='" + status + '\'' +
                ", negotiationDate=" + negotiationDate +
                ", idRequest=" + idRequest +
                ", dniAssistant='" + dniAssistant + '\'' +
                ", acceptedCustomer=" + acceptedCustomer +
                ", acceptedAssistant=" + acceptedAssistant +
                ", dniOVIUser='" + dniOVIUser + '\'' +
                ", nameOVIUser='" + nameOVIUser + '\'' +
                ", nameAssistant='" + nameAssistant + '\'' +
                '}';
    }

    private int pendingMessagesCount;

    public int getPendingMessagesCount() {
        return pendingMessagesCount;
    }

    public void setPendingMessagesCount(int pendingMessagesCount) {
        this.pendingMessagesCount = pendingMessagesCount;
    }
}
