package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ActivityDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade la actividad a la base de datos */
    public void addActivity(Activity activity) {
        jdbcTemplate.update("INSERT INTO activity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                activity.getId(), activity.getDescription(), activity.getTitle(),
                activity.getType(), activity.getMaxParticipants(), activity.getActivityDate(),
                activity.getActivityTime(), activity.getLocation(), activity.getDniInstructor());
    }

    /* Borra la actividad por su identificador */
    public void deleteActivity(int id) {
        jdbcTemplate.update("DELETE from activity where id=?", id);
    }

    /* Actualiza los datos de una actividad */
    public void updateActivity(Activity activity) {
        jdbcTemplate.update("UPDATE activity SET description=?, title=?, type=?, max_participants=?, activity_date=?, activity_time=?, location=?, dni_instructor=? where id=?",
                activity.getDescription(), activity.getTitle(), activity.getType(),
                activity.getMaxParticipants(), activity.getActivityDate(),
                activity.getActivityTime(), activity.getLocation(), activity.getDniInstructor(), activity.getId());
    }

    /* Obtiene una actividad por su ID */
    public Activity getActivity(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * from activity WHERE id=?",
                    new ActivityRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todas las actividades */
    public List<Activity> getActivities() {
        try {
            return jdbcTemplate.query("SELECT * from activity", new ActivityRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Activity>();
        }
    }

    /* Obtiene las actividades asociadas a un usuario OVI */
    public List<Activity> getActivitiesByOVIUser(String dniOVIUser) {
        try {
            return jdbcTemplate.query(
                    "SELECT DISTINCT a.* FROM activity a " +
                            "JOIN assistancelist al ON a.id = al.id_activity " +
                            "WHERE al.dni_oviuser=?",
                    new ActivityRowMapper(), dniOVIUser);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Activity>();
        }
    }
}
