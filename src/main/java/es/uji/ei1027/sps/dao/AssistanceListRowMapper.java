package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.AssistanceList;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public final class AssistanceListRowMapper implements RowMapper<AssistanceList> {
    public AssistanceList mapRow(ResultSet rs, int rowNum) throws SQLException {
        AssistanceList assistanceList = new AssistanceList();
        assistanceList.setId_list(rs.getInt("id_list"));
        assistanceList.setAssistanceDate(rs.getObject("assistance_date", LocalDate.class));
        assistanceList.setAssistanceTime(rs.getObject("assistance_time", LocalTime.class));
        assistanceList.setParticipation(rs.getBoolean("participation"));
        assistanceList.setIdActivity(rs.getInt("id_activity"));
        assistanceList.setDniAssistant(rs.getString("dni_assistant"));
        assistanceList.setDniOVIUser(rs.getString("dni_oviuser"));
        return assistanceList;
    }
}
