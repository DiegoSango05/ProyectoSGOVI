package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.AssistanceRequest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class AssistanceRequestRowMapper implements RowMapper<AssistanceRequest> {
    public AssistanceRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssistanceRequest assistanceRequest = new AssistanceRequest();
        assistanceRequest.setId(rs.getInt("id"));
        assistanceRequest.setType(rs.getString("type"));
        assistanceRequest.setDescription(rs.getString("description"));
        assistanceRequest.setSchedule(rs.getString("schedule"));
        assistanceRequest.setLocation(rs.getString("location"));
        assistanceRequest.setStatus(rs.getString("status"));
        assistanceRequest.setRequirements(rs.getString("requirements"));
        assistanceRequest.setDniOVIuser(rs.getString("dni_oviuser"));
        return assistanceRequest;
    }
}
