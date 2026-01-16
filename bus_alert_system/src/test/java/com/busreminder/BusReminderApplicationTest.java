package com.busreminder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BusReminderApplicationTest {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
        // This tests the main application class and ensures all beans are configured correctly
    }
}
