package com.openclassrooms.starterjwt.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@org.springframework.test.context.ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    public static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        if (!MYSQL.isRunning()) MYSQL.start();

        String jdbcUrlIpv4 = MYSQL.getJdbcUrl().replace("localhost", "127.0.0.1");

        registry.add("spring.datasource.url", () -> jdbcUrlIpv4);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}