package com.busreminder.service.impl;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPassengerRepository;
import com.busreminder.service.NotificationService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    @Value("${twilio.voice.url:}")
    private String twilioVoiceUrl;

    @Value("${aws.sns.region:us-east-1}")
    private String awsSnsRegion;

    @Value("${aws.sns.access-key:}")
    private String awsSnsAccessKey;

    @Value("${aws.sns.secret-key:}")
    private String awsSnsSecretKey;

    private final BusPassengerRepository busPassengerRepository;

    public NotificationServiceImpl(BusPassengerRepository busPassengerRepository) {
        this.busPassengerRepository = busPassengerRepository;
    }

    @PostConstruct
    public void init() {
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty() 
            && twilioAuthToken != null && !twilioAuthToken.isEmpty()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            logger.info("Twilio initialized successfully");
        } else {
            logger.warn("Twilio credentials not configured. SMS and calls will not be sent.");
        }
    }

    @Override
    public void sendNotifications(List<NotificationRequest> requests) {
        for (NotificationRequest request : requests) {
            sendSMS(request);
            makeCall(request);
            markAsNotified(request.getPassengerId());
        }
    }

    private void sendSMS(NotificationRequest request) {
        try {
            if (twilioAccountSid == null || twilioAccountSid.isEmpty() 
                || twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()) {
                logger.warn("Twilio not configured. Skipping SMS to {}", request.getPassengerPhone());
                return;
            }

            String messageBody = String.format(
                "Dear %s, your bus will arrive at %s in approximately %d minutes. Please be ready at the pickup point.",
                request.getPassengerName() != null ? request.getPassengerName() : "Passenger",
                request.getPickupAddress() != null ? request.getPickupAddress() : "your pickup location",
                request.getEstimatedMinutes()
            );

            Message message = Message.creator(
                new PhoneNumber(request.getPassengerPhone()),
                new PhoneNumber(twilioPhoneNumber),
                messageBody
            ).create();

            logger.info("SMS sent to {}: Message SID: {}", request.getPassengerPhone(), message.getSid());
        } catch (Exception e) {
            logger.error("Error sending SMS to {}: {}", request.getPassengerPhone(), e.getMessage(), e);
        }
    }

    private void makeCall(NotificationRequest request) {
        try {
            if (twilioAccountSid == null || twilioAccountSid.isEmpty() 
                || twilioPhoneNumber == null || twilioPhoneNumber.isEmpty()
                || twilioVoiceUrl == null || twilioVoiceUrl.isEmpty()) {
                logger.warn("Twilio not configured. Skipping call to {}", request.getPassengerPhone());
                return;
            }

            String messageText = String.format(
                "Dear %s, your bus will arrive at %s in approximately %d minutes. Please be ready at the pickup point.",
                request.getPassengerName() != null ? request.getPassengerName() : "Passenger",
                request.getPickupAddress() != null ? request.getPickupAddress() : "your pickup location",
                request.getEstimatedMinutes()
            );

            // Create call with TwiML URL or use voice URL
            Call call = Call.creator(
                new PhoneNumber(request.getPassengerPhone()),
                new PhoneNumber(twilioPhoneNumber),
                URI.create(twilioVoiceUrl)
            ).create();

            logger.info("Call initiated to {}: Call SID: {}", request.getPassengerPhone(), call.getSid());
        } catch (Exception e) {
            logger.error("Error making call to {}: {}", request.getPassengerPhone(), e.getMessage(), e);
        }
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

