package com.busreminder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    
    @Value("${kafka.topic.bus-location-updates}")
    private String busLocationUpdatesTopic;

    public String getBusLocationUpdatesTopic() {
        return busLocationUpdatesTopic;
    }
}

