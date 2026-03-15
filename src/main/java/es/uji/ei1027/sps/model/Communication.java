package es.uji.ei1027.sps.model;

import java.time.LocalDateTime;

public class Communication {
    private int id;
    private int idNegotiation;
    private String sender;
    private String message;
    private LocalDateTime messageDate;

    public Communication() {}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getIdNegotiation() {
        return idNegotiation;
    }
    public void setIdNegotiation(int idNegotiation) {
        this.idNegotiation = idNegotiation;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getMessageDate() {
        return messageDate;
    }
    public void setMessageDate(LocalDateTime messageDate) {
        this.messageDate = messageDate;
    }

    @Override
    public String toString() {
        return "Communication{" +
                "id=" + id +
                ", idNegotiation=" + idNegotiation +
                ", sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", messageDate=" + messageDate +
                '}';
    }
}