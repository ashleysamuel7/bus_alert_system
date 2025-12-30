package com.busreminder.consumer;

import com.busreminder.config.KafkaConfig;
import com.busreminder.dto.BusLocationEvent;
import com.busreminder.service.LocationProcessingService;
import com.busreminder.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BusLocationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BusLocationConsumer.class);

    private final ObjectMapper objectMapper;
    private final LocationProcessingService locationProcessingService;
    private final NotificationService notificationService;
    private final KafkaConfig kafkaConfig;

    public BusLocationConsumer(ObjectMapper objectMapper,
                               LocationProcessingService locationProcessingService,
                               NotificationService notificationService,
                               KafkaConfig kafkaConfig) {
        this.objectMapper = objectMapper;
        this.locationProcessingService = locationProcessingService;
        this.notificationService = notificationService;
        this.kafkaConfig = kafkaConfig;
    }

    @KafkaListener(topics = "${kafka.topic.bus-location-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            BusLocationEvent event = objectMapper.readValue(message, BusLocationEvent.class);
            logger.info("Received bus location event: busId={}, lat={}, lng={}", 
                    event.getBusId(), event.getLatitude(), event.getLongitude());

            // Process location and get notifications to send
            var notificationsToSend = locationProcessingService.processBusLocation(
                    event.getBusId(),
                    event.getLatitude(),
                    event.getLongitude()
            );

            // Send notifications
            if (!notificationsToSend.isEmpty()) {
                notificationService.sendNotifications(notificationsToSend);
            }

        } catch (Exception e) {
            logger.error("Error processing bus location event: {}", message, e);
        }
    }
}

