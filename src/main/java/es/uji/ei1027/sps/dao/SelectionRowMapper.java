package es.uji.ei1027.sps.dao;

import es.uji.ei1027.sps.model.Selection;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SelectionRowMapper implements RowMapper<Selection> {
    @Override
    public Selection mapRow(ResultSet rs, int rowNum) throws SQLException {
        Selection selection = new Selection();
        selection.setIdRequest(rs.getInt("id_request"));
        selection.setDniAssistant(rs.getString("dni_assistant"));
        return selection;
    }
}
