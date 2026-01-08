package com.busreminder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {OpenApiConfig.class})
@TestPropertySource(properties = {
        "spring.main.web-application-type=none"
})
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig openApiConfig;

    @Test
    void testBusReminderOpenAPI_BeanCreated() {
        // When
        OpenAPI openAPI = openApiConfig.busReminderOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        
        Info info = openAPI.getInfo();
        assertEquals("Bus Reminder System API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertTrue(info.getDescription().contains("REST API for Bus Passenger Reminder System"));
        assertNotNull(info.getContact());
        assertEquals("Bus Reminder System", info.getContact().getName());
        assertEquals("support@busreminder.com", info.getContact().getEmail());
        assertNotNull(info.getLicense());
        assertEquals("Apache 2.0", info.getLicense().getName());
    }
}

