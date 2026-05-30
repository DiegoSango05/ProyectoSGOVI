package es.uji.ei1027.sps.model;

import java.time.LocalDateTime;

public class SupportMessage {
    private int id;
    private int idSupportChat;
    private String sender;
    private String message;
    private LocalDateTime messageDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdSupportChat() {
        return idSupportChat;
    }

    public void setIdSupportChat(int idSupportChat) {
        this.idSupportChat = idSupportChat;
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
}
