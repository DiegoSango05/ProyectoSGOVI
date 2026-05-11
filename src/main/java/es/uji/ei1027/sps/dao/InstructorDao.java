package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Instructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InstructorDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade un instructor */
    public void addInstructor(Instructor instructor) {
        jdbcTemplate.update("INSERT INTO instructor VALUES(?, ?, ?)",
                instructor.getDni(), instructor.getName(), instructor.getSpecialization());
    }

    /* Borra un instructor por DNI */
    public void deleteInstructor(String dni) {
        jdbcTemplate.update("DELETE FROM instructor WHERE dni=?", dni);
    }

    /* Actualiza un instructor */
    public void updateInstructor(Instructor instructor) {
        jdbcTemplate.update("UPDATE instructor SET name=?, specialization=? WHERE dni=?",
                instructor.getName(), instructor.getSpecialization(), instructor.getDni());
    }

    /* Obtiene un instructor por DNI */
    public Instructor getInstructor(String dni) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM instructor WHERE dni=?",
                    new InstructorRowMapper(), dni);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todos los instructores */
    public List<Instructor> getInstructors() {
        try {
            return jdbcTemplate.query("SELECT * FROM instructor", new InstructorRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Instructor>();
        }
    }

    /* Obtiene instructores priorizando los que coinciden con el tipo de actividad */
    public List<Instructor> getInstructorsBySpecialty(String type) {
        try {
            // Usamos una lógica de ordenación en SQL:
            // Si specialization = type, le asignamos prioridad 1, si no 0.
            String sql = "SELECT * FROM instructor ORDER BY (specialization = ?) DESC, name ASC";
            return jdbcTemplate.query(sql, new InstructorRowMapper(), type);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Instructor>();
        }
    }
}
