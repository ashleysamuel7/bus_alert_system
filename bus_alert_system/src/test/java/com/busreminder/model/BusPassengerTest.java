package com.busreminder.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class BusPassengerTest {

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    @Test
    void testOnCreate_SetsCreatedAt() {
        // Given
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId("PNR001");
        passenger.setPassengerId("PASS001");
        passenger.setPassengerName("John Doe");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7128"));
        passenger.setPickupLongitude(new BigDecimal("-74.0060"));
        passenger.setPickupAddress("123 Main St");

        // When
        entityManager.persist(passenger);
        entityManager.flush();
        entityManager.clear();

        // Then
        BusPassenger saved = entityManager.find(BusPassenger.class, passenger.getId());
        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testOnCreate_SetsNotifiedDefault() {
        // Given
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId("PNR001");
        passenger.setPassengerId("PASS001");
        passenger.setPassengerName("John Doe");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7128"));
        passenger.setPickupLongitude(new BigDecimal("-74.0060"));
        passenger.setPickupAddress("123 Main St");

        // When
        entityManager.persist(passenger);
        entityManager.flush();
        entityManager.clear();

        // Then
        BusPassenger saved = entityManager.find(BusPassenger.class, passenger.getId());
        assertFalse(saved.getNotified());
    }

    @Test
    void testGettersAndSetters() {
        // Given
        BusPassenger passenger = new BusPassenger();
        Long id = 1L;
        String pnrId = "PNR001";
        String passengerId = "PASS001";
        String passengerName = "John Doe";
        String passengerPhone = "+1234567890";
        BigDecimal latitude = new BigDecimal("40.7128");
        BigDecimal longitude = new BigDecimal("-74.0060");
        String address = "123 Main St";
        Boolean notified = true;
        LocalDateTime notificationSentAt = LocalDateTime.now();
        LocalDateTime callMadeAt = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        passenger.setId(id);
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName(passengerName);
        passenger.setPassengerPhone(passengerPhone);
        passenger.setPickupLatitude(latitude);
        passenger.setPickupLongitude(longitude);
        passenger.setPickupAddress(address);
        passenger.setNotified(notified);
        passenger.setNotificationSentAt(notificationSentAt);
        passenger.setCallMadeAt(callMadeAt);
        passenger.setCreatedAt(createdAt);

        // Then
        assertEquals(id, passenger.getId());
        assertEquals(pnrId, passenger.getPnrId());
        assertEquals(passengerId, passenger.getPassengerId());
        assertEquals(passengerName, passenger.getPassengerName());
        assertEquals(passengerPhone, passenger.getPassengerPhone());
        assertEquals(latitude, passenger.getPickupLatitude());
        assertEquals(longitude, passenger.getPickupLongitude());
        assertEquals(address, passenger.getPickupAddress());
        assertEquals(notified, passenger.getNotified());
        assertEquals(notificationSentAt, passenger.getNotificationSentAt());
        assertEquals(callMadeAt, passenger.getCallMadeAt());
        assertEquals(createdAt, passenger.getCreatedAt());
    }
}

