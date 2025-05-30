package vitalsanity.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Profile("docker")
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        if (count != null && count > 0) {
            System.out.println("La base de datos ya está inicializada. Se omite la ejecución del script de semilla.");
            return;
        }


        ClassPathResource resource = new ClassPathResource("sql/database-script/seed_develop_db.sql");

        try (InputStream inputStream = resource.getInputStream()) {
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            jdbcTemplate.execute(sql);
            System.out.println("Script SQL ejecutado correctamente.");
        } catch (IOException e) {
            System.err.println("No se pudo leer el script SQL: " + e.getMessage());
        }
    }
}

