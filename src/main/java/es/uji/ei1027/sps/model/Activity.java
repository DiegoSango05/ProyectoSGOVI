package es.uji.ei1027.sps.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Activity {
    private int id;
    private String description;
    private String title;
    private String type;
    private int maxParticipants;
    private LocalDate activityDate;
    private LocalTime activityTime;
    private String location;
    private String dniInstructor;

    public Activity() {}

    //Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }
    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }
    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public LocalTime getActivityTime() {
        return activityTime;
    }
    public void setActivityTime(LocalTime activityTime) {
        this.activityTime = activityTime;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getDniInstructor() {
        return dniInstructor;
    }
    public void setDniInstructor(String dniInstructor) {
        this.dniInstructor = dniInstructor;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", activityDate=" + activityDate +
                ", activityTime=" + activityTime +
                ", location='" + location + '\'' +
                ", dniInstructor='" + dniInstructor + '\'' +
                '}';
    }
}
