package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommunicationDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade una comunicación */
    public void addCommunication(Communication communication) {
        jdbcTemplate.update("INSERT INTO communication VALUES(?, ?, ?, ?, ?)",
                communication.getId(), communication.getIdNegotiation(),
                communication.getSender(), communication.getMessage(),
                communication.getMessageDate());
    }

    /* Borra una comunicación */
    public void addChatMessage(Communication communication) {
        jdbcTemplate.update(
                "INSERT INTO communication (id, id_negotiation, sender, message, message_date) " +
                        "VALUES ((SELECT COALESCE(MAX(c.id), 0) + 1 FROM communication c), ?, ?, ?, ?)",
                communication.getIdNegotiation(), communication.getSender(),
                communication.getMessage(), communication.getMessageDate());
    }

    public void deleteCommunication(int id) {
        jdbcTemplate.update("DELETE FROM communication WHERE id=?", id);
    }

    /* Actualiza una comunicación */
    public void updateCommunication(Communication communication) {
        jdbcTemplate.update("UPDATE communication SET id_negotiation=?, sender=?, message=?, message_date=? WHERE id=?",
                communication.getIdNegotiation(), communication.getSender(),
                communication.getMessage(), communication.getMessageDate(),
                communication.getId());
    }

    /* Obtiene una comunicación por ID */
    public Communication getCommunication(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM communication WHERE id=?",
                    new CommunicationRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todas las comunicaciones */
    public List<Communication> getCommunications() {
        try {
            return jdbcTemplate.query("SELECT * FROM communication ORDER BY message_date ASC, id ASC", new CommunicationRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Communication>();
        }
    }

    public List<Communication> getCommunicationsByNegotiation(int idNegotiation) {
        try {
            return jdbcTemplate.query(
                    "SELECT * FROM communication WHERE id_negotiation=? ORDER BY message_date ASC, id ASC",
                    new CommunicationRowMapper(), idNegotiation);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Communication>();
        }
    }

    /* Obtiene las comunicaciones asociadas a un usuario OVI */
    public List<Communication> getCommunicationsByOVIUser(String dniOVIUser) {
        try {
            return jdbcTemplate.query(
                    "SELECT c.* FROM communication c " +
                            "JOIN negotiation n ON c.id_negotiation = n.id_negotiation " +
                            "JOIN assistancerequest ar ON n.id_request = ar.id " +
                            "WHERE ar.dni_oviuser=? " +
                            "ORDER BY c.message_date ASC, c.id ASC",
                    new CommunicationRowMapper(), dniOVIUser);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Communication>();
        }
    }
}
