package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class PAPAssistantRowMapper implements RowMapper<PAPAssistant> {
    public PAPAssistant mapRow(ResultSet rs, int rowNum) throws SQLException {
        PAPAssistant assistant = new PAPAssistant();
        assistant.setDni(rs.getString("dni"));
        assistant.setName(rs.getString("name"));
        assistant.setBirthDate(rs.getObject("birth_date", LocalDate.class));
        assistant.setAssistanceType(rs.getString("assistance_type"));
        assistant.setProfessionalTraining(rs.getString("professional_training"));
        assistant.setPreviousExperience(rs.getBoolean("previous_experience"));
        assistant.setAvailability(rs.getString("availability"));
        assistant.setLocation(rs.getString("location"));
        assistant.setStatus(rs.getString("status"));
        assistant.setPassword(rs.getString("password"));
        assistant.setPhoneNumber(rs.getString("phonenumber"));
        return assistant;
    }
}
