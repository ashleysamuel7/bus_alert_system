package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationProcessingService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Value("${google.maps.api.url:https://maps.googleapis.com/maps/api/distancematrix/json}")
    private String googleMapsApiUrl;

    @Value("${notification.threshold.minutes:10}")
    private Long notificationThresholdMinutes;

    private final PassengerService passengerService;

    public LocationProcessingService(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    public List<NotificationRequest> processBusLocation(String busId, Double busLatitude, Double busLongitude) {
        List<BusPassenger> passengers = passengerService.getUnnotifiedPassengersByBusId(busId);
        List<NotificationRequest> notificationsToSend = new ArrayList<>();

        for (BusPassenger passenger : passengers) {
            Long estimatedMinutes = calculateETA(
                    busLatitude, 
                    busLongitude, 
                    passenger.getPickupLatitude().doubleValue(), 
                    passenger.getPickupLongitude().doubleValue()
            );

            if (estimatedMinutes != null && estimatedMinutes <= notificationThresholdMinutes) {
                NotificationRequest request = new NotificationRequest();
                request.setPassengerId(passenger.getPassengerId());
                request.setPassengerName(passenger.getPassengerName());
                request.setPassengerPhone(passenger.getPassengerPhone());
                request.setPickupLatitude(passenger.getPickupLatitude());
                request.setPickupLongitude(passenger.getPickupLongitude());
                request.setPickupAddress(passenger.getPickupAddress());
                request.setEstimatedMinutes(estimatedMinutes);
                
                notificationsToSend.add(request);
            }
        }

        return notificationsToSend;
    }

    private Long calculateETA(Double originLat, Double originLng, Double destLat, Double destLng) {
        // TODO: Integrate Google Maps Distance Matrix API
        // For now, return null to indicate API integration needed
        // This will be implemented after API testing
        
        // Placeholder: Simple distance calculation (Haversine formula)
        // In production, replace with actual Google Maps API call
        return null;
    }
}

