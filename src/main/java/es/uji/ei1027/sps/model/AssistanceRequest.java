package es.uji.ei1027.sps.model;

public class AssistanceRequest {
    private int id;
    private String type;
    private String description;
    private String schedule;
    private String location;
    private String status;
    private String requirements;
    private String dniOVIuser;
    private String nameOVIuser;

    public AssistanceRequest() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchedule() {
        return schedule;
    }
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequirements() {
        return requirements;
    }
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDniOVIuser() {
        return dniOVIuser;
    }
    public void setDniOVIuser(String dniOVIuser) {
        this.dniOVIuser = dniOVIuser;
    }

    public String getNameOVIuser() {
        return nameOVIuser;
    }

    public void setNameOVIuser(String nameOVIuser) {
        this.nameOVIuser = nameOVIuser;
    }

    @Override
    public String toString() {
        return "AssistanceRequest{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", schedule='" + schedule + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", requirements='" + requirements + '\'' +
                ", dniOVIuser='" + dniOVIuser + '\'' +
                ", nameOVIuser='" + nameOVIuser + '\'' +
                '}';
    }
}
