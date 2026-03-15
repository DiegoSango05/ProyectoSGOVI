package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.AssistanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AssistanceRequestDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade la solicitud */
    public void addAssistanceRequest(AssistanceRequest assistanceRequest) {
        jdbcTemplate.update("INSERT INTO assistancerequest VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                assistanceRequest.getId(), assistanceRequest.getType(),
                assistanceRequest.getDescription(), assistanceRequest.getSchedule(),
                assistanceRequest.getLocation(), assistanceRequest.getStatus(),
                assistanceRequest.getRequirements(), assistanceRequest.getDniOVIuser());
    }

    /* Borra la solicitud */
    public void deleteAssistanceRequest(int id) {
        jdbcTemplate.update("DELETE FROM assistancerequest WHERE id=?", id);
    }

    /* Actualiza la solicitud */
    public void updateAssistanceRequest(AssistanceRequest assistanceRequest) {
        jdbcTemplate.update("UPDATE assistancerequest SET type=?, description=?, schedule=?, location=?, status=?, requirements=?, dni_oviuser=? WHERE id=?",
                assistanceRequest.getType(), assistanceRequest.getDescription(),
                assistanceRequest.getSchedule(), assistanceRequest.getLocation(),
                assistanceRequest.getStatus(), assistanceRequest.getRequirements(),
                assistanceRequest.getDniOVIuser(), assistanceRequest.getId());
    }

    /* Obtiene una solicitud por ID */
    public AssistanceRequest getAssistanceRequest(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM assistancerequest WHERE id=?",
                    new AssistanceRequestRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todas las solicitudes */
    public List<AssistanceRequest> getAssistanceRequests() {
        try {
            return jdbcTemplate.query("SELECT * FROM assistancerequest", new AssistanceRequestRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<AssistanceRequest>();
        }
    }

}
