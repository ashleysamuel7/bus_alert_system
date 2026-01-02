package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BusPassengerRepository busPassengerRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        reset(busPassengerRepository);
    }

    @Test
    void testSendNotifications() {
        // Given
        NotificationRequest request1 = createNotificationRequest("PASS001");
        NotificationRequest request2 = createNotificationRequest("PASS002");

        BusPassenger passenger1 = createPassenger("PASS001");
        BusPassenger passenger2 = createPassenger("PASS002");

        // Mock findAll() to return passengers when markAsNotified is called
        when(busPassengerRepository.findAll())
                .thenReturn(Arrays.asList(passenger1, passenger2))
                .thenReturn(Arrays.asList(passenger1, passenger2));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.sendNotifications(Arrays.asList(request1, request2));

        // Then
        ArgumentCaptor<BusPassenger> captor = ArgumentCaptor.forClass(BusPassenger.class);
        verify(busPassengerRepository, times(2)).findAll();
        verify(busPassengerRepository, times(2)).save(captor.capture());

        List<BusPassenger> savedPassengers = captor.getAllValues();
        assertEquals(2, savedPassengers.size());
        assertTrue(savedPassengers.stream().allMatch(p -> p.getNotified()));
        assertTrue(savedPassengers.stream().allMatch(p -> p.getNotificationSentAt() != null));
        assertTrue(savedPassengers.stream().allMatch(p -> p.getCallMadeAt() != null));
    }

    @Test
    void testSendNotifications_EmptyList() {
        // When
        notificationService.sendNotifications(Collections.emptyList());

        // Then
        verify(busPassengerRepository, never()).findAll();
        verify(busPassengerRepository, never()).save(any());
    }

    @Test
    void testMarkAsNotified() {
        // Given
        String passengerId = "PASS001";
        BusPassenger passenger = createPassenger(passengerId);
        passenger.setNotified(false);

        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationRequest request = createNotificationRequest(passengerId);

        // When
        notificationService.sendNotifications(Arrays.asList(request));

        // Then
        ArgumentCaptor<BusPassenger> captor = ArgumentCaptor.forClass(BusPassenger.class);
        verify(busPassengerRepository).findAll();
        verify(busPassengerRepository).save(captor.capture());

        BusPassenger savedPassenger = captor.getValue();
        assertTrue(savedPassenger.getNotified());
        assertNotNull(savedPassenger.getNotificationSentAt());
        assertNotNull(savedPassenger.getCallMadeAt());
    }

    private NotificationRequest createNotificationRequest(String passengerId) {
        NotificationRequest request = new NotificationRequest();
        request.setPassengerId(passengerId);
        request.setPassengerName("John Doe");
        request.setPassengerPhone("+1234567890");
        request.setPickupLatitude(new BigDecimal("40.7128"));
        request.setPickupLongitude(new BigDecimal("-74.0060"));
        request.setPickupAddress("123 Main St");
        request.setEstimatedMinutes(5L);
        return request;
    }

    private BusPassenger createPassenger(String passengerId) {
        BusPassenger passenger = new BusPassenger();
        passenger.setId(1L);
        passenger.setPnrId("PNR001");
        passenger.setPassengerId(passengerId);
        passenger.setPassengerName("John Doe");
        passenger.setPassengerPhone("+1234567890");
        passenger.setPickupLatitude(new BigDecimal("40.7128"));
        passenger.setPickupLongitude(new BigDecimal("-74.0060"));
        passenger.setNotified(false);
        return passenger;
    }
}
