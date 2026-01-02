package com.busreminder.consumer;

import com.busreminder.config.KafkaConfig;
import com.busreminder.dto.BusLocationEvent;
import com.busreminder.dto.NotificationRequest;
import com.busreminder.service.LocationProcessingService;
import com.busreminder.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusLocationConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LocationProcessingService locationProcessingService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private KafkaConfig kafkaConfig;

    @InjectMocks
    private BusLocationConsumer busLocationConsumer;

    @BeforeEach
    void setUp() {
        reset(objectMapper, locationProcessingService, notificationService, kafkaConfig);
    }

    @Test
    void testConsume_ValidMessage() throws Exception {
        // Given
        String message = "{\"bus_id\":\"BUS001\",\"latitude\":40.7128,\"longitude\":-74.0060,\"timestamp\":\"2024-01-15T10:30:00Z\"}";
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);
        event.setTimestamp("2024-01-15T10:30:00Z");

        NotificationRequest notificationRequest = new NotificationRequest();
        List<NotificationRequest> notifications = Arrays.asList(notificationRequest);

        when(objectMapper.readValue(message, BusLocationEvent.class)).thenReturn(event);
        when(locationProcessingService.processBusLocation(anyString(), any(Double.class), any(Double.class)))
                .thenReturn(notifications);

        // When
        busLocationConsumer.consume(message);

        // Then
        verify(objectMapper, times(1)).readValue(eq(message), eq(BusLocationEvent.class));
        verify(locationProcessingService, times(1)).processBusLocation(eq("BUS001"), eq(40.7128), eq(-74.0060));
        verify(notificationService, times(1)).sendNotifications(eq(notifications));
    }

    @Test
    void testConsume_NoNotificationsToSend() throws Exception {
        // Given
        String message = "{\"bus_id\":\"BUS001\",\"latitude\":40.7128,\"longitude\":-74.0060,\"timestamp\":\"2024-01-15T10:30:00Z\"}";
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);

        when(objectMapper.readValue(message, BusLocationEvent.class)).thenReturn(event);
        when(locationProcessingService.processBusLocation(anyString(), any(Double.class), any(Double.class)))
                .thenReturn(Collections.emptyList());

        // When
        busLocationConsumer.consume(message);

        // Then
        verify(objectMapper, times(1)).readValue(eq(message), eq(BusLocationEvent.class));
        verify(locationProcessingService, times(1)).processBusLocation(eq("BUS001"), eq(40.7128), eq(-74.0060));
        verify(notificationService, never()).sendNotifications(any());
    }

    @Test
    void testConsume_InvalidJson() throws Exception {
        // Given
        String invalidMessage = "invalid json";
        when(objectMapper.readValue(invalidMessage, BusLocationEvent.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // When
        busLocationConsumer.consume(invalidMessage);

        // Then
        verify(objectMapper, times(1)).readValue(eq(invalidMessage), eq(BusLocationEvent.class));
        verify(locationProcessingService, never()).processBusLocation(anyString(), any(Double.class), any(Double.class));
        verify(notificationService, never()).sendNotifications(any());
    }
}
