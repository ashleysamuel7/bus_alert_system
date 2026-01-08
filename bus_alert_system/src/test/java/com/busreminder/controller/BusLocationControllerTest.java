package com.busreminder.controller;

import com.busreminder.dto.BusLocationEvent;
import com.busreminder.dto.NotificationRequest;
import com.busreminder.service.LocationProcessingService;
import com.busreminder.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusLocationControllerTest {

    @Mock
    private LocationProcessingService locationProcessingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BusLocationController busLocationController;

    @BeforeEach
    void setUp() {
        reset(locationProcessingService, notificationService);
    }

    @Test
    void testUpdateBusLocation_Success() {
        // Given
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setPassengerId("PASS001");
        notificationRequest.setEstimatedMinutes(5L);
        List<NotificationRequest> notifications = Arrays.asList(notificationRequest);

        when(locationProcessingService.processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude()))
                .thenReturn(notifications);

        // When
        ResponseEntity<Map<String, Object>> response = busLocationController.updateBusLocation(event);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Bus location processed successfully", response.getBody().get("message"));
        assertEquals("BUS001", response.getBody().get("busId"));
        assertEquals(1, response.getBody().get("notificationsSent"));

        verify(locationProcessingService).processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude());
        verify(notificationService).sendNotifications(notifications);
    }

    @Test
    void testUpdateBusLocation_NoNotifications() {
        // Given
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);

        when(locationProcessingService.processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude()))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response = busLocationController.updateBusLocation(event);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(0, response.getBody().get("notificationsSent"));

        verify(locationProcessingService).processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude());
        verify(notificationService, never()).sendNotifications(anyList());
    }

    @Test
    void testUpdateBusLocation_Exception() {
        // Given
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);

        when(locationProcessingService.processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude()))
                .thenThrow(new RuntimeException("Processing error"));

        // When
        ResponseEntity<Map<String, Object>> response = busLocationController.updateBusLocation(event);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertNotNull(response.getBody().get("message"));

        verify(locationProcessingService).processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude());
        verify(notificationService, never()).sendNotifications(anyList());
    }

    @Test
    void testUpdateBusLocation_MultipleNotifications() {
        // Given
        BusLocationEvent event = new BusLocationEvent();
        event.setBusId("BUS001");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);

        NotificationRequest req1 = new NotificationRequest();
        req1.setPassengerId("PASS001");
        NotificationRequest req2 = new NotificationRequest();
        req2.setPassengerId("PASS002");
        List<NotificationRequest> notifications = Arrays.asList(req1, req2);

        when(locationProcessingService.processBusLocation(
                event.getBusId(), event.getLatitude(), event.getLongitude()))
                .thenReturn(notifications);

        // When
        ResponseEntity<Map<String, Object>> response = busLocationController.updateBusLocation(event);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("notificationsSent"));
        verify(notificationService).sendNotifications(notifications);
    }

    @Test
    void testHealth() {
        // When
        ResponseEntity<Map<String, String>> response = busLocationController.health();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("Bus Reminder System", response.getBody().get("service"));
    }
}

