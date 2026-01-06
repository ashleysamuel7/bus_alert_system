package com.busreminder.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusLocationEventTest {

    @Test
    void testGettersAndSetters() {
        // Given
        BusLocationEvent event = new BusLocationEvent();
        String busId = "BUS001";
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        String timestamp = "2024-01-15T10:30:00Z";

        // When
        event.setBusId(busId);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setTimestamp(timestamp);

        // Then
        assertEquals(busId, event.getBusId());
        assertEquals(latitude, event.getLatitude());
        assertEquals(longitude, event.getLongitude());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void testNullValues() {
        // Given
        BusLocationEvent event = new BusLocationEvent();

        // When
        event.setBusId(null);
        event.setLatitude(null);
        event.setLongitude(null);
        event.setTimestamp(null);

        // Then
        assertNull(event.getBusId());
        assertNull(event.getLatitude());
        assertNull(event.getLongitude());
        assertNull(event.getTimestamp());
    }

    @Test
    void testAllFields() {
        // Given
        BusLocationEvent event = new BusLocationEvent();

        // When
        event.setBusId("BUS002");
        event.setLatitude(40.7580);
        event.setLongitude(-73.9855);
        event.setTimestamp("2024-01-16T11:00:00Z");

        // Then
        assertEquals("BUS002", event.getBusId());
        assertEquals(40.7580, event.getLatitude());
        assertEquals(-73.9855, event.getLongitude());
        assertEquals("2024-01-16T11:00:00Z", event.getTimestamp());
    }
}


