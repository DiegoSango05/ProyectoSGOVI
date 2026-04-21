package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.AssistanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AssistanceListDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añadir a la lista de asistencia */
    public void addAssistanceList(AssistanceList assistanceList) {
        jdbcTemplate.update("INSERT INTO assistancelist VALUES(?, ?, ?, ?, ?, ?, ?)",
                assistanceList.getId_list(), assistanceList.getAssistanceDate(),
                assistanceList.getAssistanceTime(), assistanceList.isParticipation(),
                assistanceList.getIdActivity(), assistanceList.getDniAssistant(),
                assistanceList.getDniOVIUser());
    }

    /* Borrar de la lista por id_list */
    public void deleteAssistanceList(int id_list) {
        jdbcTemplate.update("DELETE FROM assistancelist WHERE id_list=?", id_list);
    }

    /* Actualizar un registro de asistencia */
    public void updateAssistanceList(AssistanceList assistanceList) {
        jdbcTemplate.update("UPDATE assistancelist SET assistance_date=?, assistance_time=?, participation=?, id_activity=?, dni_assistant=?, dni_oviuser=? WHERE id_list=?",
                assistanceList.getAssistanceDate(), assistanceList.getAssistanceTime(),
                assistanceList.isParticipation(), assistanceList.getIdActivity(),
                assistanceList.getDniAssistant(), assistanceList.getDniOVIUser(),
                assistanceList.getId_list());
    }

    /* Obtener un registro por su id_list */
    public AssistanceList getAssistanceList(int id_list) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM assistancelist WHERE id_list=?",
                    new AssistanceListRowMapper(), id_list);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtener todos los registros */
    public List<AssistanceList> getAssistanceLists() {
        try {
            return jdbcTemplate.query("SELECT * FROM assistancelist", new AssistanceListRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<AssistanceList>();
        }
    }

    /* Obtener registros por ID de Actividad */
    public List<AssistanceList> getAssistanceListsByActivity(int idActivity) {
        try {
            return jdbcTemplate.query("SELECT * FROM assistancelist WHERE id_activity=?",
                    new AssistanceListRowMapper(), idActivity);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<AssistanceList>();
        }
    }
}
