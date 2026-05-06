package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContractDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* Añade un contrato */
    public void addContract(Contract contract) {
        jdbcTemplate.update("INSERT INTO contract VALUES(?, ?, ?, ?, ?, ?)",
                contract.getId(), contract.getStartDate(), contract.getEndDate(),
                contract.getStatus(), contract.getDocument(), contract.getIdNegotiation());
    }

    /* Borra un contrato */
    public void deleteContract(int id) {
        jdbcTemplate.update("DELETE FROM contract WHERE id=?", id);
    }

    /* Actualiza un contrato */
    public void updateContract(Contract contract) {
        jdbcTemplate.update("UPDATE contract SET start_date=?, end_date=?, status=?, document=?, id_negotiation=? WHERE id=?",
                contract.getStartDate(), contract.getEndDate(), contract.getStatus(),
                contract.getDocument(), contract.getIdNegotiation(), contract.getId());
    }

    /* Obtiene un contrato por ID */
    public Contract getContract(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM contract WHERE id=?",
                    new ContractRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /* Obtiene todos los contratos */
    public List<Contract> getContracts() {
        try {
            return jdbcTemplate.query("SELECT * FROM contract", new ContractRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Contract>();
        }
    }

    /* Obtiene los contratos asociados a las solicitudes de un usuario OVI */
    public List<Contract> getContractsByOVIUser(String dniOVIUser) {
        try {
            return jdbcTemplate.query(
                    "SELECT c.* FROM contract c " +
                            "JOIN negotiation n ON c.id_negotiation = n.id_negotiation " +
                            "JOIN assistancerequest ar ON n.id_request = ar.id " +
                            "WHERE ar.dni_oviuser=?",
                    new ContractRowMapper(), dniOVIUser);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Contract>();
        }
    }
}
