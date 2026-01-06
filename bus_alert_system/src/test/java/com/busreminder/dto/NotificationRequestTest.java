package com.busreminder.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    @Test
    void testGettersAndSetters() {
        // Given
        NotificationRequest request = new NotificationRequest();
        String passengerId = "PASS001";
        String passengerName = "John Doe";
        String passengerPhone = "+1234567890";
        BigDecimal pickupLatitude = new BigDecimal("40.7128");
        BigDecimal pickupLongitude = new BigDecimal("-74.0060");
        String pickupAddress = "123 Main St";
        Long estimatedMinutes = 10L;

        // When
        request.setPassengerId(passengerId);
        request.setPassengerName(passengerName);
        request.setPassengerPhone(passengerPhone);
        request.setPickupLatitude(pickupLatitude);
        request.setPickupLongitude(pickupLongitude);
        request.setPickupAddress(pickupAddress);
        request.setEstimatedMinutes(estimatedMinutes);

        // Then
        assertEquals(passengerId, request.getPassengerId());
        assertEquals(passengerName, request.getPassengerName());
        assertEquals(passengerPhone, request.getPassengerPhone());
        assertEquals(pickupLatitude, request.getPickupLatitude());
        assertEquals(pickupLongitude, request.getPickupLongitude());
        assertEquals(pickupAddress, request.getPickupAddress());
        assertEquals(estimatedMinutes, request.getEstimatedMinutes());
    }

    @Test
    void testNullValues() {
        // Given
        NotificationRequest request = new NotificationRequest();

        // When
        request.setPassengerId(null);
        request.setPassengerName(null);
        request.setPassengerPhone(null);
        request.setPickupLatitude(null);
        request.setPickupLongitude(null);
        request.setPickupAddress(null);
        request.setEstimatedMinutes(null);

        // Then
        assertNull(request.getPassengerId());
        assertNull(request.getPassengerName());
        assertNull(request.getPassengerPhone());
        assertNull(request.getPickupLatitude());
        assertNull(request.getPickupLongitude());
        assertNull(request.getPickupAddress());
        assertNull(request.getEstimatedMinutes());
    }

    @Test
    void testAllFields() {
        // Given
        NotificationRequest request = new NotificationRequest();

        // When
        request.setPassengerId("PASS002");
        request.setPassengerName("Jane Smith");
        request.setPassengerPhone("+1987654321");
        request.setPickupLatitude(new BigDecimal("40.7580"));
        request.setPickupLongitude(new BigDecimal("-73.9855"));
        request.setPickupAddress("456 Park Ave");
        request.setEstimatedMinutes(5L);

        // Then
        assertEquals("PASS002", request.getPassengerId());
        assertEquals("Jane Smith", request.getPassengerName());
        assertEquals("+1987654321", request.getPassengerPhone());
        assertEquals(new BigDecimal("40.7580"), request.getPickupLatitude());
        assertEquals(new BigDecimal("-73.9855"), request.getPickupLongitude());
        assertEquals("456 Park Ave", request.getPickupAddress());
        assertEquals(5L, request.getEstimatedMinutes());
    }
}


