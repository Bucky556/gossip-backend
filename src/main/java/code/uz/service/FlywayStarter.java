package code.uz.service;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class FlywayStarter implements CommandLineRunner {
    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        Flyway.configure().baselineOnMigrate(true).dataSource(dataSource).load().migrate();
    }
}
