package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.PAPAssistant;
import es.uji.ei1027.sps.model.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SelectionDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade una nueva selección (Recomendación del Admin) */
    public void addSelection(Selection selection) {
        jdbcTemplate.update("INSERT INTO selection (id_request, dni_assistant) VALUES (?, ?)",
                selection.getIdRequest(), selection.getDniAssistant());
    }

    /* Borra todas las selecciones de una solicitud específica */
    public void deleteSelectionsByRequest(int idRequest) {
        jdbcTemplate.update("DELETE FROM selection WHERE id_request = ?", idRequest);
    }

    /* Borra una selección específica */
    public void deleteSelection(int idRequest, String dniAssistant) {
        jdbcTemplate.update("DELETE FROM selection WHERE id_request = ? AND dni_assistant = ?",
                idRequest, dniAssistant);
    }

    /* Obtiene los asistentes recomendados para una solicitud (Retorna lista de PAPAssistant)
       Este es el que usarás para mostrar las tarjetas al usuario OVI */
    public List<PAPAssistant> getAssistantsForRequest(int idRequest) {
        String sql = "SELECT a.* FROM pap_assistant a " +
                "JOIN selection s ON a.dni = s.dni_assistant " +
                "WHERE s.id_request = ?";
        return jdbcTemplate.query(sql, new PAPAssistantRowMapper(), idRequest);
    }

    /* Obtiene todas las selecciones (objetos Selection) */
    public List<Selection> getSelections() {
        return jdbcTemplate.query("SELECT * FROM selection", new SelectionRowMapper());
    }
}
