package com.busreminder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "busLocationUpdatesTopic", "bus-location-updates");
    }

    @Test
    void testGetBusLocationUpdatesTopic() {
        // When
        String topic = kafkaConfig.getBusLocationUpdatesTopic();

        // Then
        assertEquals("bus-location-updates", topic);
    }

    @Test
    void testObjectMapperBean() {
        // When
        ObjectMapper objectMapper = kafkaConfig.objectMapper();

        // Then
        assertNotNull(objectMapper);
        assertInstanceOf(ObjectMapper.class, objectMapper);
    }
}


