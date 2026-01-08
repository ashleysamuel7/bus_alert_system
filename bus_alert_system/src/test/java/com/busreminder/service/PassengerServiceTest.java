package com.busreminder.service;

import com.busreminder.model.BusPnr;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPnrRepository;
import com.busreminder.repository.BusPassengerRepository;
import com.busreminder.service.impl.PassengerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private BusPnrRepository busPnrRepository;

    @Mock
    private BusPassengerRepository busPassengerRepository;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    @BeforeEach
    void setUp() {
        reset(busPnrRepository, busPassengerRepository);
    }

    @Test
    void testGetPassengersByBusId() {
        // Given
        String busId = "BUS001";
        BusPnr pnr1 = createBusPnr("BUS001", "PNR001");
        BusPnr pnr2 = createBusPnr("BUS001", "PNR002");
        
        BusPassenger passenger1 = createPassenger("PNR001", "PASS001");
        BusPassenger passenger2 = createPassenger("PNR001", "PASS002");
        BusPassenger passenger3 = createPassenger("PNR002", "PASS003");

        when(busPnrRepository.findByBusId(busId)).thenReturn(Arrays.asList(pnr1, pnr2));
        when(busPassengerRepository.findByPnrIdIn(Arrays.asList("PNR001", "PNR002")))
                .thenReturn(Arrays.asList(passenger1, passenger2, passenger3));

        // When
        List<BusPassenger> result = passengerService.getPassengersByBusId(busId);

        // Then
        assertEquals(3, result.size());
        verify(busPnrRepository).findByBusId(busId);
        verify(busPassengerRepository).findByPnrIdIn(Arrays.asList("PNR001", "PNR002"));
    }

    @Test
    void testGetPassengersByBusId_WhenNoPnrs() {
        // Given
        String busId = "BUS999";
        when(busPnrRepository.findByBusId(busId)).thenReturn(Collections.emptyList());

        // When
        List<BusPassenger> result = passengerService.getPassengersByBusId(busId);

        // Then
        assertTrue(result.isEmpty());
        verify(busPnrRepository).findByBusId(busId);
        verify(busPassengerRepository, never()).findByPnrIdIn(anyList());
    }

    @Test
    void testGetUnnotifiedPassengersByBusId() {
        // Given
        String busId = "BUS001";
        BusPnr pnr1 = createBusPnr("BUS001", "PNR001");
        
        BusPassenger passenger1 = createPassenger("PNR001", "PASS001");

        when(busPnrRepository.findByBusId(busId)).thenReturn(Arrays.asList(pnr1));
        when(busPassengerRepository.findByPnrIdInAndNotifiedFalse(Arrays.asList("PNR001")))
                .thenReturn(Arrays.asList(passenger1));

        // When
        List<BusPassenger> result = passengerService.getUnnotifiedPassengersByBusId(busId);

        // Then
        assertEquals(1, result.size());
        assertEquals("PASS001", result.get(0).getPassengerId());
        verify(busPnrRepository).findByBusId(busId);
        verify(busPassengerRepository).findByPnrIdInAndNotifiedFalse(Arrays.asList("PNR001"));
    }

    private BusPnr createBusPnr(String busId, String pnrId) {
        BusPnr busPnr = new BusPnr();
        busPnr.setBusId(busId);
        busPnr.setPnrId(pnrId);
        return busPnr;
    }

    private BusPassenger createPassenger(String pnrId, String passengerId) {
        BusPassenger passenger = new BusPassenger();
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName("Test Passenger");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7128"));
        passenger.setPickupLongitude(new BigDecimal("-74.0060"));
        return passenger;
    }
}
