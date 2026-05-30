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

    public void addNegotiation(Negotiation negotiation) {
        String sql = "INSERT INTO negotiation (status, negotiation_date, id_request, dni_assistant, accepted_customer, accepted_assistant) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                negotiation.getStatus(),
                negotiation.getNegotiationDate(),
                negotiation.getIdRequest(),
                negotiation.getDniAssistant(),
                negotiation.isAcceptedCustomer(),
                negotiation.isAcceptedAssistant());
    }

    public int addNegotiationAndReturnId(Negotiation negotiation) {
        String sql = "INSERT INTO negotiation (status, negotiation_date, id_request, dni_assistant, accepted_customer, accepted_assistant) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_negotiation";
        return jdbcTemplate.queryForObject(sql, Integer.class,
                negotiation.getStatus(),
                negotiation.getNegotiationDate(),
                negotiation.getIdRequest(),
                negotiation.getDniAssistant(),
                negotiation.isAcceptedCustomer(),
                negotiation.isAcceptedAssistant());
    }

    /* Borra una negociación */
    public void deleteNegotiation(int idNegotiation) {
        jdbcTemplate.update("DELETE FROM negotiation WHERE id_negotiation=?", idNegotiation);
    }

    /* Actualiza una negociación incluyendo las nuevas banderas */
    public void updateNegotiation(Negotiation negotiation) {
        String sql = "UPDATE negotiation SET status=?, negotiation_date=?, id_request=?, dni_assistant=?, accepted_customer=?, accepted_assistant=? WHERE id_negotiation=?";
        jdbcTemplate.update(sql,
                negotiation.getStatus(), negotiation.getNegotiationDate(),
                negotiation.getIdRequest(), negotiation.getDniAssistant(),
                negotiation.isAcceptedCustomer(), negotiation.isAcceptedAssistant(), // 🆕
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

    public List<Negotiation> getActiveNegotiationsByOVIUser(String dniOVIUser) {
        try {
            return jdbcTemplate.query(
                    "SELECT n.* FROM negotiation n " +
                            "JOIN assistancerequest ar ON n.id_request = ar.id " +
                            "WHERE ar.dni_oviuser=? " +
                            "AND (n.status IS NULL OR LOWER(n.status) <> 'rejected') " +
                            "ORDER BY n.negotiation_date DESC, n.id_negotiation DESC",
                    new NegotiationRowMapper(), dniOVIUser);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Negotiation>();
        }
    }

    public List<Negotiation> getActiveNegotiationsByAssistant(String dniAssistant) {
        try {
            return jdbcTemplate.query(
                    "SELECT n.* FROM negotiation n " +
                            "WHERE n.dni_assistant=? " +
                            "AND (n.status IS NULL OR LOWER(n.status) <> 'rejected') " +
                            "ORDER BY n.negotiation_date DESC, n.id_negotiation DESC",
                    new NegotiationRowMapper(), dniAssistant);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Negotiation>();
        }
    }

    public List<Negotiation> getMutualAgreements() {
        try {
            String sql = "SELECT * FROM negotiation WHERE accepted_customer = true AND accepted_assistant = true AND LOWER(status) = 'pending'";
            return jdbcTemplate.query(sql, new NegotiationRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Negotiation>();
        }
    }

    public void updateCustomerAcceptance(int idNegotiation, boolean accepted) {
        String sql = "UPDATE negotiation SET accepted_customer = ? WHERE id_negotiation = ?";
        jdbcTemplate.update(sql, accepted, idNegotiation);
    }

    public void updateAssistantAcceptance(int idNegotiation, boolean accepted) {
        String sql = "UPDATE negotiation SET accepted_assistant = ? WHERE id_negotiation = ?";
        jdbcTemplate.update(sql, accepted, idNegotiation);
    }
}