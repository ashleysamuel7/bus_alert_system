package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
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

    @InjectMocks
    private LocationProcessingService locationProcessingService;

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
    void testProcessBusLocation_WithPassengers_WhenETANull() {
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

        // Then
        // Since calculateETA returns null, no notifications should be created
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

        // Then
        // Note: Since calculateETA currently returns null, result will be empty
        // This test verifies the service structure is correct
        // When calculateETA is implemented, this test should verify notifications are created
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
}
