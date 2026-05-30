package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.SupportChat;
import es.uji.ei1027.sps.model.SupportMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SupportChatDao {
    private static final String ADMIN_SENDER = "Administrador";
    private static final String WELCOME_MESSAGE =
            "Bienvenido/a a la app SgOVI. Soy el administrador. No dudes en contactar conmigo en caso de cualquier problema.";

    private JdbcTemplate jdbcTemplate;
    private boolean schemaReady;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createWelcomeChatIfAbsent(String participantDni, String participantType, String participantName) {
        ensureSchema();
        List<Integer> ids = jdbcTemplate.queryForList(
                "INSERT INTO support_chat (participant_dni, participant_type, participant_name) " +
                        "VALUES (?, ?, ?) ON CONFLICT (participant_dni, participant_type) DO NOTHING RETURNING id",
                Integer.class, participantDni, participantType, participantName);

        if (!ids.isEmpty()) {
            addMessage(ids.get(0), ADMIN_SENDER, WELCOME_MESSAGE);
        }
    }

    public List<SupportChat> getSupportChats() {
        ensureSchema();
        return jdbcTemplate.query(
                "SELECT * FROM support_chat ORDER BY participant_name ASC, id ASC",
                (rs, rowNum) -> mapChat(rs.getInt("id"), rs.getString("participant_dni"),
                        rs.getString("participant_type"), rs.getString("participant_name")));
    }

    public List<SupportChat> getSupportChatsByParticipant(String participantDni, String participantType) {
        ensureSchema();
        return jdbcTemplate.query(
                "SELECT * FROM support_chat WHERE participant_dni=? AND participant_type=? ORDER BY id ASC",
                (rs, rowNum) -> mapChat(rs.getInt("id"), rs.getString("participant_dni"),
                        rs.getString("participant_type"), rs.getString("participant_name")),
                participantDni, participantType);
    }

    public List<SupportMessage> getMessagesByChat(int idSupportChat) {
        ensureSchema();
        return jdbcTemplate.query(
                "SELECT * FROM support_message WHERE id_support_chat=? ORDER BY message_date ASC, id ASC",
                (rs, rowNum) -> {
                    SupportMessage message = new SupportMessage();
                    message.setId(rs.getInt("id"));
                    message.setIdSupportChat(rs.getInt("id_support_chat"));
                    message.setSender(rs.getString("sender"));
                    message.setMessage(rs.getString("message"));
                    message.setMessageDate(rs.getObject("message_date", LocalDateTime.class));
                    return message;
                },
                idSupportChat);
    }

    public void addMessage(int idSupportChat, String sender, String message) {
        ensureSchema();
        jdbcTemplate.update(
                "INSERT INTO support_message (id_support_chat, sender, message, message_date) VALUES (?, ?, ?, ?)",
                idSupportChat, sender, message, LocalDateTime.now());
    }

    private synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS support_chat (" +
                        "id SERIAL PRIMARY KEY, " +
                        "participant_dni VARCHAR(30) NOT NULL, " +
                        "participant_type VARCHAR(20) NOT NULL, " +
                        "participant_name VARCHAR(150) NOT NULL, " +
                        "UNIQUE (participant_dni, participant_type))");
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS support_message (" +
                        "id SERIAL PRIMARY KEY, " +
                        "id_support_chat INTEGER NOT NULL REFERENCES support_chat(id) ON DELETE CASCADE, " +
                        "sender VARCHAR(150) NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "message_date TIMESTAMP NOT NULL)");
        schemaReady = true;
    }

    private SupportChat mapChat(int id, String participantDni, String participantType, String participantName) {
        SupportChat chat = new SupportChat();
        chat.setId(id);
        chat.setParticipantDni(participantDni);
        chat.setParticipantType(participantType);
        chat.setParticipantName(participantName);
        return chat;
    }
}
