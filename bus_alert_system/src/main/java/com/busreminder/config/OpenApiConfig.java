package com.busreminder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI busReminderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bus Reminder System API")
                        .description("REST API for Bus Passenger Reminder System. " +
                                "Receives bus location updates and triggers notifications to passengers " +
                                "when buses approach pickup points.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bus Reminder System")
                                .email("support@busreminder.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

