package com.busreminder.controller;

import com.busreminder.dto.BusLocationEvent;
import com.busreminder.service.LocationProcessingService;
import com.busreminder.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bus-location")
@Tag(name = "Bus Location", description = "API for updating bus locations and triggering passenger notifications")
public class BusLocationController {

    private static final Logger logger = LoggerFactory.getLogger(BusLocationController.class);

    private final LocationProcessingService locationProcessingService;
    private final NotificationService notificationService;

    public BusLocationController(LocationProcessingService locationProcessingService,
                                 NotificationService notificationService) {
        this.locationProcessingService = locationProcessingService;
        this.notificationService = notificationService;
    }

    @Operation(
        summary = "Update bus location",
        description = "Receives bus location update, calculates ETAs for passengers, and sends notifications if within threshold"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bus location processed successfully",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = Map.class))
        )
    })
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateBusLocation(@RequestBody BusLocationEvent event) {
        try {
            logger.info("Received bus location update via REST: busId={}, lat={}, lng={}", 
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bus location processed successfully");
            response.put("busId", event.getBusId());
            response.put("notificationsSent", notificationsToSend.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing bus location update: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(
        summary = "Health check",
        description = "Returns the health status of the service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service is healthy",
        content = @Content(schema = @Schema(implementation = Map.class))
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Bus Reminder System");
        return ResponseEntity.ok(response);
    }
}

