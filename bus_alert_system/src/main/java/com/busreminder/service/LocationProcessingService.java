package com.busreminder.service;

import com.busreminder.dto.NotificationRequest;
import com.busreminder.model.BusPassenger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(LocationProcessingService.class);

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Value("${google.maps.api.url:https://maps.googleapis.com/maps/api/distancematrix/json}")
    private String googleMapsApiUrl;

    @Value("${notification.threshold.minutes:10}")
    private Long notificationThresholdMinutes;

    private final PassengerService passengerService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LocationProcessingService(PassengerService passengerService, ObjectMapper objectMapper) {
        this.passengerService = passengerService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
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
        // If API key is not configured, fall back to Haversine formula
        if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
            logger.debug("Google Maps API key not configured, using Haversine formula for ETA calculation");
            return calculateETAHaversine(originLat, originLng, destLat, destLng);
        }

        try {
            // Build Google Maps Distance Matrix API URL
            String origin = originLat + "," + originLng;
            String destination = destLat + "," + destLng;
            
            String url = String.format("%s?origins=%s&destinations=%s&key=%s&units=imperial",
                    googleMapsApiUrl,
                    URLEncoder.encode(origin, StandardCharsets.UTF_8),
                    URLEncoder.encode(destination, StandardCharsets.UTF_8),
                    URLEncoder.encode(googleMapsApiKey, StandardCharsets.UTF_8));

            // Make HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse JSON response
            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                
                // Check for API errors
                String status = jsonNode.path("status").asText();
                if (!"OK".equals(status)) {
                    String errorMessage = jsonNode.path("error_message").asText("Unknown error");
                    logger.warn("Google Maps API returned status: {} - {}", status, errorMessage);
                    return calculateETAHaversine(originLat, originLng, destLat, destLng);
                }

                // Extract duration from response
                JsonNode rows = jsonNode.path("rows");
                if (rows.isArray() && rows.size() > 0) {
                    JsonNode elements = rows.get(0).path("elements");
                    if (elements.isArray() && elements.size() > 0) {
                        JsonNode element = elements.get(0);
                        String elementStatus = element.path("status").asText();
                        
                        if ("OK".equals(elementStatus)) {
                            // Get duration in seconds from "duration" field
                            JsonNode duration = element.path("duration");
                            if (duration.has("value")) {
                                long durationSeconds = duration.path("value").asLong();
                                // Convert to minutes
                                long durationMinutes = (durationSeconds + 30) / 60; // Round up
                                logger.debug("Calculated ETA: {} minutes ({} seconds)", durationMinutes, durationSeconds);
                                return durationMinutes;
                            }
                        } else {
                            logger.warn("Google Maps API element status: {}", elementStatus);
                        }
                    }
                }
            } else {
                logger.error("Google Maps API returned status code: {}", response.statusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling Google Maps Distance Matrix API: {}", e.getMessage(), e);
        }

        // Fall back to Haversine formula if API call fails
        logger.debug("Falling back to Haversine formula for ETA calculation");
        return calculateETAHaversine(originLat, originLng, destLat, destLng);
    }

    /**
     * Calculate ETA using Haversine formula (great circle distance)
     * Assumes average speed of 30 mph (48 km/h) for urban areas
     */
    private Long calculateETAHaversine(Double originLat, Double originLng, Double destLat, Double destLng) {
        final double EARTH_RADIUS_MILES = 3958.8; // Earth radius in miles
        final double AVERAGE_SPEED_MPH = 30.0; // Average urban speed in miles per hour

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(originLat);
        double lat2Rad = Math.toRadians(destLat);
        double deltaLatRad = Math.toRadians(destLat - originLat);
        double deltaLngRad = Math.toRadians(destLng - originLng);

        // Haversine formula
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMiles = EARTH_RADIUS_MILES * c;

        // Calculate time in minutes (distance / speed * 60)
        long estimatedMinutes = Math.round((distanceMiles / AVERAGE_SPEED_MPH) * 60);
        
        logger.debug("Haversine calculation: distance={} miles, ETA={} minutes", 
                String.format("%.2f", distanceMiles), estimatedMinutes);
        
        return estimatedMinutes;
    }
}

