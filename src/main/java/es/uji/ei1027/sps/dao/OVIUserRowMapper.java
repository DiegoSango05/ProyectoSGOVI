package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.OVIUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class OVIUserRowMapper implements RowMapper<OVIUser> {
    public OVIUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        OVIUser oviUser = new OVIUser();
        oviUser.setDni(rs.getString("dni"));
        oviUser.setName(rs.getString("name"));
        oviUser.setBirthDate(rs.getObject("birthdate", LocalDate.class));
        oviUser.setEmail(rs.getString("email"));
        oviUser.setPhoneNumber(rs.getString("phonenumber"));
        oviUser.setAddress(rs.getString("address"));
        oviUser.setEmergencyContact(rs.getString("emergencycontact"));
        oviUser.setDocument(rs.getString("document"));
        oviUser.setPassword(rs.getString("password"));
        return oviUser;
    }
}
