package com.busreminder.config;

import com.busreminder.model.BusPnr;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPnrRepository;
import com.busreminder.repository.BusPassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private BusPnrRepository busPnrRepository;

    @Mock
    private BusPassengerRepository busPassengerRepository;

    @InjectMocks
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        reset(busPnrRepository, busPassengerRepository);
    }

    @Test
    void testRun_WhenDataExists() throws Exception {
        // Given
        when(busPnrRepository.count()).thenReturn(5L);

        // When
        dataLoader.run();

        // Then - should return early without loading data
        verify(busPnrRepository).count();
        verify(busPnrRepository, never()).save(any(BusPnr.class));
        verify(busPassengerRepository, never()).save(any(BusPassenger.class));
    }

    @Test
    void testRun_WhenDataDoesNotExist() throws Exception {
        // Given
        // First call returns 0 (no data exists), subsequent calls return loaded counts
        when(busPnrRepository.count()).thenReturn(0L, 20L);
        when(busPassengerRepository.count()).thenReturn(200L);
        when(busPnrRepository.save(any(BusPnr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataLoader.run();

        // Then - should load data for 10 buses
        verify(busPnrRepository, atLeastOnce()).count();
        verify(busPnrRepository, atLeastOnce()).save(any(BusPnr.class));
        verify(busPassengerRepository, atLeastOnce()).save(any(BusPassenger.class));
    }

    @Test
    void testLoadBusData() throws Exception {
        // Given
        when(busPnrRepository.count()).thenReturn(0L, 20L);
        when(busPassengerRepository.count()).thenReturn(200L);
        when(busPnrRepository.save(any(BusPnr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataLoader.run();

        // Then - verify bus data structure
        ArgumentCaptor<BusPnr> pnrCaptor = ArgumentCaptor.forClass(BusPnr.class);
        ArgumentCaptor<BusPassenger> passengerCaptor = ArgumentCaptor.forClass(BusPassenger.class);

        verify(busPnrRepository, atLeastOnce()).save(pnrCaptor.capture());
        verify(busPassengerRepository, atLeastOnce()).save(passengerCaptor.capture());

        List<BusPnr> savedPnrs = pnrCaptor.getAllValues();
        List<BusPassenger> savedPassengers = passengerCaptor.getAllValues();

        // Verify PNR structure
        assertFalse(savedPnrs.isEmpty());
        savedPnrs.forEach(pnr -> {
            assertNotNull(pnr.getBusId());
            assertNotNull(pnr.getPnrId());
            assertTrue(pnr.getBusId().startsWith("BUS"));
        });

        // Verify passenger structure
        assertFalse(savedPassengers.isEmpty());
        savedPassengers.forEach(passenger -> {
            assertNotNull(passenger.getPnrId());
            assertNotNull(passenger.getPassengerId());
            assertNotNull(passenger.getPassengerName());
            assertNotNull(passenger.getPassengerPhone());
            assertNotNull(passenger.getPickupLatitude());
            assertNotNull(passenger.getPickupLongitude());
            assertNotNull(passenger.getPickupAddress());
            assertFalse(passenger.getNotified());
        });
    }

    @Test
    void testCreatePassenger() throws Exception {
        // Given
        when(busPnrRepository.count()).thenReturn(0L, 20L);
        when(busPassengerRepository.count()).thenReturn(200L);
        when(busPnrRepository.save(any(BusPnr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataLoader.run();

        // Then - verify passenger creation with all fields
        ArgumentCaptor<BusPassenger> captor = ArgumentCaptor.forClass(BusPassenger.class);
        verify(busPassengerRepository, atLeastOnce()).save(captor.capture());

        List<BusPassenger> passengers = captor.getAllValues();
        assertFalse(passengers.isEmpty());

        BusPassenger passenger = passengers.get(0);
        assertNotNull(passenger.getPnrId());
        assertNotNull(passenger.getPassengerId());
        assertNotNull(passenger.getPassengerName());
        assertNotNull(passenger.getPassengerPhone());
        assertTrue(passenger.getPassengerPhone().startsWith("+1"));
        assertNotNull(passenger.getPickupLatitude());
        assertNotNull(passenger.getPickupLongitude());
        assertNotNull(passenger.getPickupAddress());
        assertTrue(passenger.getPickupAddress().contains("New York, NY"));
        assertFalse(passenger.getNotified());
    }

    @Test
    void testDistributePassengers() throws Exception {
        // Given
        when(busPnrRepository.count()).thenReturn(0L, 20L);
        when(busPassengerRepository.count()).thenReturn(200L);
        when(busPnrRepository.save(any(BusPnr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataLoader.run();

        // Then - verify passenger distribution logic
        // Each bus should have approximately 20 passengers distributed across 2-4 PNRs
        ArgumentCaptor<BusPnr> pnrCaptor = ArgumentCaptor.forClass(BusPnr.class);
        ArgumentCaptor<BusPassenger> passengerCaptor = ArgumentCaptor.forClass(BusPassenger.class);

        verify(busPnrRepository, atLeastOnce()).save(pnrCaptor.capture());
        verify(busPassengerRepository, atLeastOnce()).save(passengerCaptor.capture());

        List<BusPnr> savedPnrs = pnrCaptor.getAllValues();
        List<BusPassenger> savedPassengers = passengerCaptor.getAllValues();

        // Verify that passengers are distributed across PNRs
        assertFalse(savedPnrs.isEmpty());
        assertFalse(savedPassengers.isEmpty());

        // Verify that each PNR has at least one passenger
        savedPnrs.forEach(pnr -> {
            long passengerCount = savedPassengers.stream()
                    .filter(p -> p.getPnrId().equals(pnr.getPnrId()))
                    .count();
            assertTrue(passengerCount > 0, "Each PNR should have at least one passenger");
        });
    }
}


