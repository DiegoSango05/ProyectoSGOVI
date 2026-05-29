package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.AssistanceRequest;
import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PAPAssistantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade un asistente PAP (Modificado para especificar columnas y evitar fallos de estructura) */
    public void addPAPAssistant(PAPAssistant assistant) {
        String sql = "INSERT INTO pap_assistant (dni, name, birth_date, assistance_type, professional_training, " +
                "previous_experience, availability, location, status, password, phonenumber, rejection_reason) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                assistant.getDni(),
                assistant.getName(),
                assistant.getBirthDate(),
                assistant.getAssistanceType(),
                assistant.getProfessionalTraining(),
                assistant.isPreviousExperience(),
                assistant.getAvailability(),
                assistant.getLocation(),
                assistant.getStatus(),
                assistant.getPassword(),
                assistant.getPhoneNumber(),
                assistant.getRejectionReason()); // Será null al crearse, lo cual es correcto
    }

    /* Borra un asistente por DNI */
    public void deletePAPAssistant(String dni) {
        jdbcTemplate.update("DELETE FROM pap_assistant WHERE dni=?", dni);
    }

    /* Actualiza un asistente (Incluye el motivo del rechazo) */
    public void updatePAPAssistant(PAPAssistant assistant) {
        jdbcTemplate.update(
                "UPDATE pap_assistant SET name=?, birth_date=?, assistance_type=?, professional_training=?, previous_experience=?, availability=?, location=?, status=?, password=?, phonenumber=?, rejection_reason=? WHERE dni=?",
                assistant.getName(),
                assistant.getBirthDate(),
                assistant.getAssistanceType(),
                assistant.getProfessionalTraining(),
                assistant.isPreviousExperience(),
                assistant.getAvailability(),
                assistant.getLocation(),
                assistant.getStatus(),
                assistant.getPassword(),
                assistant.getPhoneNumber(),
                assistant.getRejectionReason(), // Guardamos el motivo en la BD
                assistant.getDni()
        );
    }

    /* Obtiene un asistente por DNI */
    public PAPAssistant getPAPAssistant(String dni) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM pap_assistant WHERE dni=?",
                    new PAPAssistantRowMapper(), dni);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todos los asistentes */
    public List<PAPAssistant> getPAPAssistants() {
        try {
            return jdbcTemplate.query("SELECT * FROM pap_assistant", new PAPAssistantRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<PAPAssistant>();
        }
    }

    /* Login real */
    public PAPAssistant loadUserByUsername(String dni, String password) {
        PAPAssistant assistant = getPAPAssistant(dni);
        if (assistant == null) return null;

        if (assistant.getPassword().equals(password)) {
            assistant.setPassword(null);
            return assistant;
        }
        return null;
    }

    /* Obtiene los asistentes recomendados para una petición específica */
    public List<PAPAssistant> getCandidatesForRequest(AssistanceRequest request) {
        String sql = "SELECT * FROM pap_assistant " +
                "WHERE status = 'Accepted' " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN location = ? AND availability = ? AND assistance_type = ? THEN 1 " +
                "    WHEN location = ? AND availability = ? THEN 2 " +
                "    ELSE 3 " +
                "  END ASC, name ASC";

        try {
            return jdbcTemplate.query(sql, new PAPAssistantRowMapper(),
                    request.getLocation(), request.getSchedule(), request.getType(),
                    request.getLocation(), request.getSchedule());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
}