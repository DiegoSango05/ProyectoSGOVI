package es.uji.ei1027.sps.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AssistanceList {
    private int id_list;
    private LocalDate assistanceDate;
    private LocalTime assistanceTime;
    private boolean participation;
    private int idActivity;
    private String dniAssistant;
    private String dniOVIUser;

    public AssistanceList() {}

    public int getId_list() {
        return id_list;
    }
    public void setId_list(int id_list) {
        this.id_list = id_list;
    }

    public LocalDate getAssistanceDate() {
        return assistanceDate;
    }
    public void setAssistanceDate(LocalDate assistanceDate) {
        this.assistanceDate = assistanceDate;
    }

    public LocalTime getAssistanceTime() {
        return assistanceTime;
    }
    public void setAssistanceTime(LocalTime assistanceTime) {
        this.assistanceTime = assistanceTime;
    }

    public boolean isParticipation() {
        return participation;
    }
    public void setParticipation(boolean participation) {
        this.participation = participation;
    }

    public int getIdActivity() {
        return idActivity;
    }
    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public String getDniAssistant() {
        return dniAssistant;
    }
    public void setDniAssistant(String dniAssistant) {
        this.dniAssistant = dniAssistant;
    }

    public String getDniOVIUser() {
        return dniOVIUser;
    }
    public void setDniOVIUser(String dniOVIUser) {
        this.dniOVIUser = dniOVIUser;
    }

    @Override
    public String toString() {
        return "AssistanceList{" +
                "id_list=" + id_list +
                ", assistanceDate=" + assistanceDate +
                ", assistanceTime=" + assistanceTime +
                ", participation=" + participation +
                ", idActivity=" + idActivity +
                ", dniAssistant=" + dniAssistant +
                ", dniOVIUser='" + dniOVIUser + '\'' +
                '}';
    }
}
