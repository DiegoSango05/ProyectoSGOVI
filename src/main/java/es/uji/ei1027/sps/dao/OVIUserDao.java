package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.OVIUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OVIUserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade un usuario OVI - Ahora incluye el campo 'status' por defecto como 'Pending' */
    public void addOVIUser(OVIUser oviUser) {
        jdbcTemplate.update("INSERT INTO oviuser VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                oviUser.getDni(),
                oviUser.getName(),
                oviUser.getBirthDate(),
                oviUser.getEmail(),
                oviUser.getPhoneNumber(),
                oviUser.getAddress(),
                oviUser.getEmergencyContact(),
                oviUser.getDocument(),
                oviUser.getPassword(),
                "Pending");
    }

    /* Borra un usuario por DNI */
    public void deleteOVIUser(String dni) {
        jdbcTemplate.update("DELETE FROM oviuser WHERE dni=?", dni);
    }

    /* Actualiza un usuario incluyendo el nuevo campo status */
    public void updateOVIUser(OVIUser oviUser) {
        jdbcTemplate.update("UPDATE oviuser SET name=?, birthdate=?, email=?, phonenumber=?, address=?, emergencycontact=?, document=?, password=?, status=? WHERE dni=?",
                oviUser.getName(),
                oviUser.getBirthDate(),
                oviUser.getEmail(),
                oviUser.getPhoneNumber(),
                oviUser.getAddress(),
                oviUser.getEmergencyContact(),
                oviUser.getDocument(),
                oviUser.getPassword(),
                oviUser.getStatus(), // Nuevo campo en el update
                oviUser.getDni());
    }

    /* Obtiene un usuario por DNI */
    public OVIUser getOVIUser(String dni) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM oviuser WHERE dni=?",
                    new OVIUserRowMapper(), dni);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todos los usuarios */
    public List<OVIUser> getOVIUsers() {
        try {
            return jdbcTemplate.query("SELECT * FROM oviuser", new OVIUserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<OVIUser>();
        }
    }

    /* Login real - Es importante comprobar el status en el controller después de esto */
    public OVIUser loadUserByUsername(String dni, String password) {
        OVIUser user = getOVIUser(dni);
        if (user == null) return null;

        if (user.getPassword().equals(password)) {
            // Nota: Aquí podrías filtrar si el status es 'Rejected' para ni siquiera dejarle entrar
            user.setPassword(null);
            return user;
        }
        return null;
    }
}