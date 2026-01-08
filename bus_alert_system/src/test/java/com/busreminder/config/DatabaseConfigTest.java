package com.busreminder.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {DatabaseConfig.class})
@TestPropertySource(properties = {
        "spring.main.web-application-type=none"
})
class DatabaseConfigTest {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Test
    void testDatabaseConfig_BeanCreated() {
        // Then
        assertNotNull(databaseConfig);
    }
}

