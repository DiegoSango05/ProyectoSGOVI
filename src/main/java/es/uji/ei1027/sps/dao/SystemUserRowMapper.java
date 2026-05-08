package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.SystemUser;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemUserRowMapper implements RowMapper<SystemUser> {
    @Override
    public SystemUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        SystemUser user = new SystemUser();
        user.setId(rs.getString("id"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setName(rs.getString("name"));
        return user;
    }
}
