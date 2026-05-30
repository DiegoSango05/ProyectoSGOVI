package es.uji.ei1027.sps.model;

import java.time.LocalDate;

public class PAPAssistant {
    private String dni;
    private String name;
    private LocalDate birthDate;
    private String assistanceType;
    private String professionalTraining;
    private boolean previousExperience;
    private String availability;
    private String location;
    private String status;
    private String password;
    private String phoneNumber;
    private String rejectionReason;
    private String confirmPassword;
    private Boolean acceptedPrivacyPolicy;


    public PAPAssistant() {}

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getAssistanceType() {
        return assistanceType;
    }
    public void setAssistanceType(String assistanceType) {
        this.assistanceType = assistanceType;
    }

    public String getProfessionalTraining() {
        return professionalTraining;
    }
    public void setProfessionalTraining(String professionalTraining) {
        this.professionalTraining = professionalTraining;
    }

    public boolean isPreviousExperience() {
        return previousExperience;
    }
    public void setPreviousExperience(boolean previousExperience) {
        this.previousExperience = previousExperience;
    }

    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean getAcceptedPrivacyPolicy() {
        return acceptedPrivacyPolicy;
    }
    public void setAcceptedPrivacyPolicy(Boolean acceptedPrivacyPolicy) {
        this.acceptedPrivacyPolicy = acceptedPrivacyPolicy;
    }
    @Override
    public String toString() {
        return "PAPAssistant{" +
                "dni='" + dni + '\'' +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", assistanceType='" + assistanceType + '\'' +
                ", professionalTraining='" + professionalTraining + '\'' +
                ", previousExperience=" + previousExperience +
                ", availability='" + availability + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
