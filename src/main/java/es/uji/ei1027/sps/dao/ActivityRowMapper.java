package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Activity;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ActivityRowMapper implements RowMapper<Activity> {
    public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getInt("id"));
        activity.setDescription(rs.getString("description"));
        activity.setTitle(rs.getString("title"));
        activity.setType(rs.getString("type"));
        activity.setMaxParticipants(rs.getInt("max_participants"));
        activity.setActivityDate(rs.getObject("activity_date", LocalDate.class));
        activity.setActivityTime(rs.getObject("activity_time", LocalTime.class));
        activity.setLocation(rs.getString("location"));
        activity.setDniInstructor(rs.getString("dni_instructor"));
        return activity;
    }

}
