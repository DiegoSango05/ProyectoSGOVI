package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Instructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class InstructorRowMapper implements RowMapper<Instructor> {
    public Instructor mapRow(ResultSet rs, int rowNum) throws SQLException {
        Instructor instructor = new Instructor();
        instructor.setDni(rs.getString("dni"));
        instructor.setName(rs.getString("name"));
        instructor.setSpecialization(rs.getString("specialization"));
        return instructor;
    }
}
