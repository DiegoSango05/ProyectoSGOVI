package es.uji.ei1027.sps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DbTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testQueryAdmin() {
        System.out.println("=== ADMINISTRATORS ===");
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM administrator");
            for (Map<String, Object> row : list) {
                System.out.println(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=== SUPPORT CHATS ===");
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM support_chat");
            for (Map<String, Object> row : list) {
                System.out.println(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
