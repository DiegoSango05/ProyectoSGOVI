package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class SystemUserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Busca un administrador por sus credenciales.
     * Devuelve el objeto SystemUser si coincide, o null si no existe.
     */
    public SystemUser loadUserByUsername(String username, String password) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, password, role, name FROM administrator WHERE id=? AND password=?",
                    new SystemUserRowMapper(),
                    username, password
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
