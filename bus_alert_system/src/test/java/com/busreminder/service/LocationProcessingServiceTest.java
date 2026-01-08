package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import com.busreminder.service.impl.LocationProcessingServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationProcessingServiceTest {

    @Mock
    private PassengerService passengerService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LocationProcessingServiceImpl locationProcessingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(locationProcessingService, "notificationThresholdMinutes", 10L);
        reset(passengerService);
    }

    @Test
    void testProcessBusLocation_ReturnsEmptyList() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Collections.emptyList());

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then
        assertTrue(result.isEmpty());
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    @Test
    void testProcessBusLocation_WithPassengers_WhenETAWithinThreshold() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;

        BusPassenger passenger = createPassenger("PNR001", "PASS001");

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Arrays.asList(passenger));

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then - ETA is calculated (~7 minutes) which is within 10-minute threshold
        assertFalse(result.isEmpty(), "Notifications should be created when ETA is within threshold");
        assertEquals(1, result.size());
        NotificationRequest request = result.get(0);
        assertEquals("PASS001", request.getPassengerId());
        assertNotNull(request.getEstimatedMinutes());
        assertTrue(request.getEstimatedMinutes() <= 10, "ETA should be within threshold");
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    @Test
    void testProcessBusLocation_WithPassengers_WhenETAExactlyAtThreshold() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;
        
        // Set threshold to match calculated ETA (~7 minutes)
        ReflectionTestUtils.setField(locationProcessingService, "notificationThresholdMinutes", 7L);

        BusPassenger passenger = createPassenger("PNR001", "PASS001");

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Arrays.asList(passenger));

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then - ETA is exactly at threshold, should create notification
        assertFalse(result.isEmpty(), "Notifications should be created when ETA equals threshold");
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEstimatedMinutes() <= 7);
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    @Test
    void testProcessBusLocation_ETAExceedsThreshold() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;
        
        // Use passenger coordinates far away (e.g., ~20 miles = ~40 minutes ETA)
        BusPassenger passenger = createPassengerFarAway("PNR001", "PASS001");

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Arrays.asList(passenger));

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then - ETA exceeds 10-minute threshold, no notifications should be created
        assertTrue(result.isEmpty(), "No notifications should be created when ETA exceeds threshold");
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    @Test
    void testProcessBusLocation_WithPassengers_WhenETABelowThreshold() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;
        
        // Set threshold to 5 minutes (below calculated ETA of ~7 minutes)
        ReflectionTestUtils.setField(locationProcessingService, "notificationThresholdMinutes", 5L);

        BusPassenger passenger = createPassenger("PNR001", "PASS001");

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Arrays.asList(passenger));

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then - ETA (~7 minutes) exceeds threshold (5 minutes), no notifications created
        assertTrue(result.isEmpty(), "No notifications when ETA exceeds threshold");
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    @Test
    void testCalculateETA_ReturnsActualValue() {
        // Given
        Double originLat = 40.7128;
        Double originLng = -74.0060;
        Double destLat = 40.7580;
        Double destLng = -73.9855;

        // When - invoke private method using ReflectionTestUtils
        Long result = (Long) ReflectionTestUtils.invokeMethod(
                locationProcessingService, 
                "calculateETA", 
                originLat, originLng, destLat, destLng
        );

        // Then - verify implementation calculates actual ETA using Haversine formula
        // Distance ~5 miles at 30 mph = ~7 minutes
        assertNotNull(result);
        assertTrue(result > 0, "ETA should be positive");
        assertTrue(result <= 10, "ETA should be within reasonable range for nearby locations");
    }

    @Test
    void testProcessBusLocation_MultiplePassengers() {
        // Given
        String busId = "BUS001";
        Double busLatitude = 40.7128;
        Double busLongitude = -74.0060;

        BusPassenger passenger1 = createPassenger("PNR001", "PASS001");
        BusPassenger passenger2 = createPassenger("PNR002", "PASS002");

        when(passengerService.getUnnotifiedPassengersByBusId(busId))
                .thenReturn(Arrays.asList(passenger1, passenger2));

        // When
        List<NotificationRequest> result = locationProcessingService.processBusLocation(
                busId, busLatitude, busLongitude);

        // Then - Both passengers have ETA within threshold, should get notifications
        assertFalse(result.isEmpty(), "Notifications should be created for passengers within threshold");
        assertEquals(2, result.size(), "Both passengers should receive notifications");
        
        // Verify both passengers are in the result
        List<String> passengerIds = result.stream()
                .map(NotificationRequest::getPassengerId)
                .toList();
        assertTrue(passengerIds.contains("PASS001"));
        assertTrue(passengerIds.contains("PASS002"));
        
        // Verify ETAs are within threshold
        result.forEach(request -> {
            assertNotNull(request.getEstimatedMinutes());
            assertTrue(request.getEstimatedMinutes() <= 10, "ETA should be within threshold");
        });
        
        verify(passengerService).getUnnotifiedPassengersByBusId(busId);
    }

    private BusPassenger createPassenger(String pnrId, String passengerId) {
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName("John Doe");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7580"));
        passenger.setPickupLongitude(new BigDecimal("-73.9855"));
        passenger.setPickupAddress("123 Main St");
        passenger.setNotified(false);
        return passenger;
    }

    private BusPassenger createPassengerFarAway(String pnrId, String passengerId) {
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName("John Doe");
        passenger.setPassengerPhone("+1234567890");
        // Use coordinates far away (~20 miles = ~40 minutes ETA at 30 mph)
        // Example: Philadelphia area from NYC
        passenger.setPickupLatitude(new BigDecimal("40.0000"));
        passenger.setPickupLongitude(new BigDecimal("-75.0000"));
        passenger.setPickupAddress("Far Away Location");
        passenger.setNotified(false);
        return passenger;
    }
}
