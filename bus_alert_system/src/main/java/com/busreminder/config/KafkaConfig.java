package com.busreminder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    
    @Value("${kafka.topic.bus-location-updates}")
    private String busLocationUpdatesTopic;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public String getBusLocationUpdatesTopic() {
        return busLocationUpdatesTopic;
    }
}

