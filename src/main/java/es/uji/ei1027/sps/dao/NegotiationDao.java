package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Negotiation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NegotiationDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade una negociación */
    public void addNegotiation(Negotiation negotiation) {
        jdbcTemplate.update("INSERT INTO negotiation VALUES(?, ?, ?, ?, ?)",
                negotiation.getIdNegotiation(), negotiation.getStatus(),
                negotiation.getNegotiationDate(), negotiation.getIdRequest(),
                negotiation.getDniAssistant());
    }

    /* Borra una negociación */
    public void deleteNegotiation(int idNegotiation) {
        jdbcTemplate.update("DELETE FROM negotiation WHERE id_negotiation=?", idNegotiation);
    }

    /* Actualiza una negociación */
    public void updateNegotiation(Negotiation negotiation) {
        jdbcTemplate.update("UPDATE negotiation SET status=?, negotiation_date=?, id_request=?, dni_assistant=? WHERE id_negotiation=?",
                negotiation.getStatus(), negotiation.getNegotiationDate(),
                negotiation.getIdRequest(), negotiation.getDniAssistant(),
                negotiation.getIdNegotiation());
    }

    /* Obtiene una negociación por ID */
    public Negotiation getNegotiation(int idNegotiation) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM negotiation WHERE id_negotiation=?",
                    new NegotiationRowMapper(), idNegotiation);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todas las negociaciones */
    public List<Negotiation> getNegotiations() {
        try {
            return jdbcTemplate.query("SELECT * FROM negotiation", new NegotiationRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Negotiation>();
        }
    }
}
