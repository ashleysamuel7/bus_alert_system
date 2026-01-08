package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;

import java.util.List;

/**
 * Service interface for processing bus locations and calculating ETAs.
 */
public interface LocationProcessingService {
    
    /**
     * Process bus location and return list of notifications to send.
     * 
     * @param busId Bus identifier
     * @param busLatitude Bus current latitude
     * @param busLongitude Bus current longitude
     * @return List of notification requests for passengers within threshold
     */
    List<NotificationRequest> processBusLocation(String busId, Double busLatitude, Double busLongitude);
}

