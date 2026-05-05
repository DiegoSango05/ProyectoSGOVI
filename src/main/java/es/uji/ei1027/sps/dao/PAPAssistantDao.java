package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class    PAPAssistantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade un asistente PAP */
    public void addPAPAssistant(PAPAssistant assistant) {
        jdbcTemplate.update("INSERT INTO pap_assistant VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                assistant.getDni(), assistant.getName(), assistant.getBirthDate(),
                assistant.getAssistanceType(), assistant.getProfessionalTraining(),
                assistant.isPreviousExperience(), assistant.getAvailability(),
                assistant.getLocation(), assistant.getStatus(), assistant.getPassword());
    }

    /* Borra un asistente por DNI */
    public void deletePAPAssistant(String dni) {
        jdbcTemplate.update("DELETE FROM pap_assistant WHERE dni=?", dni);
    }

    /* Actualiza un asistente */
    public void updatePAPAssistant(PAPAssistant assistant) {
        jdbcTemplate.update("UPDATE pap_assistant SET name=?, birth_date=?, assistance_type=?, professional_training=?, previous_experience=?, availability=?, location=?, status=?, password=? WHERE dni=?",
                assistant.getName(), assistant.getBirthDate(), assistant.getAssistanceType(),
                assistant.getProfessionalTraining(), assistant.isPreviousExperience(),
                assistant.getAvailability(), assistant.getLocation(), assistant.getStatus(),
                assistant.getPassword(), assistant.getDni());
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
}
