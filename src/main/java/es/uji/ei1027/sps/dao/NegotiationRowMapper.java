package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Negotiation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class NegotiationRowMapper implements RowMapper<Negotiation> {
    public Negotiation mapRow(ResultSet rs, int rowNum) throws SQLException {
        Negotiation negotiation = new Negotiation();
        negotiation.setIdNegotiation(rs.getInt("id_negotiation"));
        negotiation.setStatus(rs.getString("status"));
        negotiation.setNegotiationDate(rs.getObject("negotiation_date", LocalDate.class));
        negotiation.setIdRequest(rs.getInt("id_request"));
        negotiation.setDniAssistant(rs.getString("dni_assistant"));
        negotiation.setAcceptedCustomer(rs.getBoolean("accepted_customer"));
        negotiation.setAcceptedAssistant(rs.getBoolean("accepted_assistant"));
        return negotiation;
    }
}
