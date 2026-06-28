package es.uji.ei1027.sps.model;

import java.time.LocalDate;

public class Contract {
    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String document;
    private int idNegotiation;
    private String dniOVIUser;
    private String nameOVIUser;
    private String dniAssistant;
    private String nameAssistant;

    public Contract() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocument() {
        return document;
    }
    public void setDocument(String document) {
        this.document = document;
    }

    public int getIdNegotiation() {
        return idNegotiation;
    }
    public void setIdNegotiation(int idNegotiation) {
        this.idNegotiation = idNegotiation;
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

    public String getDniAssistant() {
        return dniAssistant;
    }
    public void setDniAssistant(String dniAssistant) {
        this.dniAssistant = dniAssistant;
    }

    public String getNameAssistant() {
        return nameAssistant;
    }
    public void setNameAssistant(String nameAssistant) {
        this.nameAssistant = nameAssistant;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", document='" + document + '\'' +
                ", idNegotiation=" + idNegotiation +
                ", dniOVIUser='" + dniOVIUser + '\'' +
                ", nameOVIUser='" + nameOVIUser + '\'' +
                ", dniAssistant='" + dniAssistant + '\'' +
                ", nameAssistant='" + nameAssistant + '\'' +
                '}';
    }
}
