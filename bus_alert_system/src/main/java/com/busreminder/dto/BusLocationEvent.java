package com.busreminder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bus location event containing bus ID, coordinates, and timestamp")
public class BusLocationEvent {
    
    @JsonProperty("bus_id")
    @Schema(description = "Unique bus identifier", example = "BUS001", required = true)
    private String busId;
    
    @JsonProperty("latitude")
    @Schema(description = "Bus latitude coordinate", example = "40.7128", required = true)
    private Double latitude;
    
    @JsonProperty("longitude")
    @Schema(description = "Bus longitude coordinate", example = "-74.0060", required = true)
    private Double longitude;
    
    @JsonProperty("timestamp")
    @Schema(description = "Event timestamp in ISO 8601 format", example = "2024-01-15T10:30:00Z")
    private String timestamp;

    // Getters and Setters
    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

