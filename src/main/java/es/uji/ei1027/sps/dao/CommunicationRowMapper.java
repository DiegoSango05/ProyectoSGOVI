package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Communication;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class CommunicationRowMapper implements RowMapper<Communication> {
    public Communication mapRow(ResultSet rs, int rowNum) throws SQLException {
        Communication communication = new Communication();
        communication.setId(rs.getInt("id"));
        communication.setIdNegotiation(rs.getInt("id_negotiation"));
        communication.setSender(rs.getString("sender"));
        communication.setMessage(rs.getString("message"));
        communication.setMessageDate(rs.getObject("message_date", LocalDateTime.class));
        return communication;
    }
}
