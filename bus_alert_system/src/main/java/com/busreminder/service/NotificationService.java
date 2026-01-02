package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPassengerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Value("${aws.sns.region:us-east-1}")
    private String awsSnsRegion;

    @Value("${aws.sns.access-key:}")
    private String awsSnsAccessKey;

    @Value("${aws.sns.secret-key:}")
    private String awsSnsSecretKey;

    private final BusPassengerRepository busPassengerRepository;

    public NotificationService(BusPassengerRepository busPassengerRepository) {
        this.busPassengerRepository = busPassengerRepository;
    }

    public void sendNotifications(List<NotificationRequest> requests) {
        for (NotificationRequest request : requests) {
            sendSMS(request);
            makeCall(request);
            markAsNotified(request.getPassengerId());
        }
    }

    private void sendSMS(NotificationRequest request) {
        // TODO: Integrate AWS SNS for SMS
        // This will be implemented after API testing
        
        String message = String.format(
            "Dear %s, your bus will arrive at %s in approximately %d minutes. Please be ready at the pickup point.",
            request.getPassengerName() != null ? request.getPassengerName() : "Passenger",
            request.getPickupAddress() != null ? request.getPickupAddress() : "your pickup location",
            request.getEstimatedMinutes()
        );
        
        // Placeholder for SMS sending
        System.out.println("SMS to " + request.getPassengerPhone() + ": " + message);
    }

    private void makeCall(NotificationRequest request) {
        // TODO: Integrate Twilio Voice API, AWS Connect, or similar
        // This will be implemented after API testing
        
        // Placeholder for phone call
        System.out.println("Calling " + request.getPassengerPhone() + " for passenger " + request.getPassengerId());
    }

    private void markAsNotified(String passengerId) {
        List<BusPassenger> passengers = busPassengerRepository.findAll().stream()
                .filter(p -> p.getPassengerId().equals(passengerId))
                .toList();
        
        for (BusPassenger passenger : passengers) {
            passenger.setNotified(true);
            passenger.setNotificationSentAt(LocalDateTime.now());
            passenger.setCallMadeAt(LocalDateTime.now());
            busPassengerRepository.save(passenger);
        }
    }
}

