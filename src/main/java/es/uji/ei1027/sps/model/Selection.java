package es.uji.ei1027.sps.model;

public class Selection {
    private int idSelection; // Nuevo campo
    private int idRequest;
    private String dniAssistant;

    public Selection() {}

    // Getters y Setters para el nuevo campo
    public int getIdSelection() { return idSelection; }
    public void setIdSelection(int idSelection) { this.idSelection = idSelection; }

    public int getIdRequest() { return idRequest; }
    public void setIdRequest(int idRequest) { this.idRequest = idRequest; }

    public String getDniAssistant() { return dniAssistant; }
    public void setDniAssistant(String dniAssistant) { this.dniAssistant = dniAssistant; }

    @Override
    public String toString() {
        return "Selection{" +
                "idSelection=" + idSelection +
                ", idRequest=" + idRequest +
                ", dniAssistant='" + dniAssistant + '\'' +
                '}';
    }
}
