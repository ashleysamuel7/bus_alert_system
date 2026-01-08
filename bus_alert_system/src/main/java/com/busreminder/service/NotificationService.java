package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;

import java.util.List;

/**
 * Service interface for sending notifications to passengers.
 */
public interface NotificationService {
    
    /**
     * Send notifications (SMS and voice calls) to passengers.
     * 
     * @param requests List of notification requests to process
     */
    void sendNotifications(List<NotificationRequest> requests);
}
