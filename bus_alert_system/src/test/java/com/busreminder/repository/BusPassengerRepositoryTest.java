package com.busreminder.repository;

import com.busreminder.model.BusPassenger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class BusPassengerRepositoryTest {

    @Autowired
    private BusPassengerRepository busPassengerRepository;

    @Test
    void testFindByPnrIdIn() {
        // Given
        BusPassenger passenger1 = createPassenger("PNR001", "PASS001", false);
        BusPassenger passenger2 = createPassenger("PNR001", "PASS002", false);
        BusPassenger passenger3 = createPassenger("PNR002", "PASS003", false);
        BusPassenger passenger4 = createPassenger("PNR003", "PASS004", false);
        
        busPassengerRepository.saveAll(Arrays.asList(passenger1, passenger2, passenger3, passenger4));

        // When
        List<BusPassenger> result = busPassengerRepository.findByPnrIdIn(Arrays.asList("PNR001", "PNR002"));

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getPnrId().equals("PNR001") && p.getPassengerId().equals("PASS001")));
        assertTrue(result.stream().anyMatch(p -> p.getPnrId().equals("PNR001") && p.getPassengerId().equals("PASS002")));
        assertTrue(result.stream().anyMatch(p -> p.getPnrId().equals("PNR002") && p.getPassengerId().equals("PASS003")));
        assertTrue(result.stream().allMatch(p -> p.getPnrId().equals("PNR001") || p.getPnrId().equals("PNR002")));
    }

    @Test
    void testFindByPnrIdInAndNotifiedFalse() {
        // Given
        BusPassenger passenger1 = createPassenger("PNR001", "PASS001", false);
        BusPassenger passenger2 = createPassenger("PNR001", "PASS002", true);
        BusPassenger passenger3 = createPassenger("PNR002", "PASS003", false);
        
        busPassengerRepository.saveAll(Arrays.asList(passenger1, passenger2, passenger3));

        // When
        List<BusPassenger> result = busPassengerRepository.findByPnrIdInAndNotifiedFalse(Arrays.asList("PNR001", "PNR002"));

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> !p.getNotified()));
        assertTrue(result.stream().anyMatch(p -> p.getPassengerId().equals("PASS001")));
        assertTrue(result.stream().anyMatch(p -> p.getPassengerId().equals("PASS003")));
        assertTrue(result.stream().noneMatch(p -> p.getPassengerId().equals("PASS002")));
    }

    private BusPassenger createPassenger(String pnrId, String passengerId, Boolean notified) {
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName("Test Passenger");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7128"));
        passenger.setPickupLongitude(new BigDecimal("-74.0060"));
        passenger.setPickupAddress("123 Test St");
        passenger.setNotified(notified);
        return passenger;
    }
}
