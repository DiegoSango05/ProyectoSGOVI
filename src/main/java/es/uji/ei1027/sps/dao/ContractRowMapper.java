package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Contract;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ContractRowMapper implements RowMapper<Contract> {
    public Contract mapRow(ResultSet rs, int rowNum) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getInt("id"));
        contract.setStartDate(rs.getObject("startdate", LocalDate.class));
        contract.setEndDate(rs.getObject("enddate", LocalDate.class));
        contract.setStatus(rs.getString("status"));
        contract.setDocument(rs.getString("document"));
        contract.setIdNegotiation(rs.getInt("id_negotiation"));
        return contract;
    }
}
