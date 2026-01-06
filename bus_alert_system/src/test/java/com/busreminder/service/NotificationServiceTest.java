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
import org.springframework.test.util.ReflectionTestUtils;

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
        // Reset Twilio fields
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "");
        ReflectionTestUtils.setField(notificationService, "twilioAuthToken", "");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "");
        ReflectionTestUtils.setField(notificationService, "twilioVoiceUrl", "");
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

    @Test
    void testInit_WithTwilioCredentials() {
        // Given
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "AC1234567890");
        ReflectionTestUtils.setField(notificationService, "twilioAuthToken", "auth_token_123");

        // When
        ReflectionTestUtils.invokeMethod(notificationService, "init");

        // Then - verify no exception is thrown and Twilio is initialized
        // Note: We can't easily verify Twilio.init() was called without PowerMockito,
        // but we can verify the method completes successfully
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(notificationService, "init"));
    }

    @Test
    void testInit_WithoutTwilioCredentials() {
        // Given - credentials are empty (set in setUp)

        // When
        ReflectionTestUtils.invokeMethod(notificationService, "init");

        // Then - verify no exception is thrown
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(notificationService, "init"));
    }

    @Test
    void testSendSMS_MissingConfig() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "");

        // When
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify markAsNotified is still called (SMS skipped but notification marked)
        verify(busPassengerRepository, atLeastOnce()).findAll();
    }

    @Test
    void testSendSMS_Exception() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "AC123");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "+1234567890");
        
        BusPassenger passenger = createPassenger("PASS001");
        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - SMS will fail due to invalid credentials, but should not throw exception
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify markAsNotified is still called despite SMS failure
        verify(busPassengerRepository, atLeastOnce()).findAll();
        verify(busPassengerRepository, atLeastOnce()).save(any(BusPassenger.class));
    }

    @Test
    void testMakeCall_MissingConfig() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "");
        ReflectionTestUtils.setField(notificationService, "twilioVoiceUrl", "");

        // When
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify markAsNotified is still called (call skipped but notification marked)
        verify(busPassengerRepository, atLeastOnce()).findAll();
    }

    @Test
    void testMakeCall_Exception() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "AC123");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "+1234567890");
        ReflectionTestUtils.setField(notificationService, "twilioVoiceUrl", "invalid-url");
        
        BusPassenger passenger = createPassenger("PASS001");
        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Call will fail due to invalid URL, but should not throw exception
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify markAsNotified is still called despite call failure
        verify(busPassengerRepository, atLeastOnce()).findAll();
        verify(busPassengerRepository, atLeastOnce()).save(any(BusPassenger.class));
    }

    @Test
    void testMarkAsNotified_NoPassengersFound() {
        // Given
        String passengerId = "PASS999";
        NotificationRequest request = createNotificationRequest(passengerId);
        
        when(busPassengerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        notificationService.sendNotifications(Arrays.asList(request));

        // Then
        verify(busPassengerRepository).findAll();
        verify(busPassengerRepository, never()).save(any(BusPassenger.class));
    }

    @Test
    void testMarkAsNotified_MultiplePassengers() {
        // Given
        String passengerId = "PASS001";
        NotificationRequest request = createNotificationRequest(passengerId);
        
        BusPassenger passenger1 = createPassenger(passengerId);
        passenger1.setPnrId("PNR001");
        BusPassenger passenger2 = createPassenger(passengerId);
        passenger2.setPnrId("PNR002");
        
        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger1, passenger2));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - both passengers should be marked as notified
        ArgumentCaptor<BusPassenger> captor = ArgumentCaptor.forClass(BusPassenger.class);
        verify(busPassengerRepository).findAll();
        verify(busPassengerRepository, times(2)).save(captor.capture());
        
        List<BusPassenger> savedPassengers = captor.getAllValues();
        assertEquals(2, savedPassengers.size());
        assertTrue(savedPassengers.stream().allMatch(p -> p.getNotified()));
        assertTrue(savedPassengers.stream().allMatch(p -> p.getNotificationSentAt() != null));
        assertTrue(savedPassengers.stream().allMatch(p -> p.getCallMadeAt() != null));
    }

    @Test
    void testSendSMS_WithNullValues() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        request.setPassengerName(null);
        request.setPickupAddress(null);
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "AC123");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "+1234567890");
        
        BusPassenger passenger = createPassenger("PASS001");
        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - should handle null values gracefully
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify no exception thrown
        verify(busPassengerRepository, atLeastOnce()).findAll();
    }

    @Test
    void testMakeCall_WithNullValues() {
        // Given
        NotificationRequest request = createNotificationRequest("PASS001");
        request.setPassengerName(null);
        request.setPickupAddress(null);
        ReflectionTestUtils.setField(notificationService, "twilioAccountSid", "AC123");
        ReflectionTestUtils.setField(notificationService, "twilioPhoneNumber", "+1234567890");
        ReflectionTestUtils.setField(notificationService, "twilioVoiceUrl", "https://demo.twilio.com/welcome/voice/");
        
        BusPassenger passenger = createPassenger("PASS001");
        when(busPassengerRepository.findAll()).thenReturn(Arrays.asList(passenger));
        when(busPassengerRepository.save(any(BusPassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - should handle null values gracefully
        notificationService.sendNotifications(Arrays.asList(request));

        // Then - verify no exception thrown
        verify(busPassengerRepository, atLeastOnce()).findAll();
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
