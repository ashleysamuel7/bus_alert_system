# Bus Passenger Reminder System

## Overview

Spring Boot backend that automates bus arrival notifications. Receives real-time bus locations via Kafka, calculates ETAs using Google Maps API, and sends SMS/calls to passengers when buses approach pickup points.

**Scale**: 500 buses, 25,000 passengers (~50 passengers/bus)

---

## Architecture

```
Kafka (bus_id, lat, lng, timestamp)
    ↓
Spring Boot App
    ├─ BusLocationConsumer → LocationProcessingService
    │   └─ Query bus_pnr → Query bus_passenger → Calculate ETA
    └─ NotificationService (Twilio SMS/Calls)
    ↓
MySQL (bus_pnr, bus_passenger)
```

---

## PNR (Passenger Name Record)

Industry-standard booking reference grouping passengers by transaction:
- **Structure**: `bus_id` → `pnr_id` → `passengers` (one-to-many-to-many)
- **Example**: BUS001 has PNR001 (4 passengers), PNR002 (2 passengers), PNR003 (1 passenger)
- **Benefits**: Batch operations, efficient querying, shared pickup points

---

## Core Components

1. **BusLocationConsumer**: Consumes Kafka events (bus_id, latitude, longitude, timestamp)
2. **LocationProcessingService**: 
   - Fetches pnr_ids for bus_id → passengers for pnr_ids
   - Calculates ETA via Google Maps Distance Matrix API
   - Filters by notification threshold (default: 10 minutes)
3. **NotificationService**: Sends SMS/calls via Twilio, marks passengers as notified
4. **PassengerService**: Database queries (bus_id → pnr_id → passengers)

---

## Data Flow

1. Kafka event received: `bus_id`, `latitude`, `longitude`, `timestamp`
2. Query `bus_pnr` for pnr_ids → Query `bus_passenger` for passengers
3. Calculate ETA for each passenger pickup point (Google Maps API)
4. Send notifications to passengers within threshold, mark as notified

---

## Database Schema

### bus_pnr
```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT,
bus_id VARCHAR(50) NOT NULL,
pnr_id VARCHAR(50) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY uk_bus_pnr (bus_id, pnr_id),
INDEX idx_bus_id (bus_id)
```

### bus_passenger
```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT,
pnr_id VARCHAR(50) NOT NULL,
passenger_id VARCHAR(50) NOT NULL,
passenger_name VARCHAR(100),
passenger_phone VARCHAR(20) NOT NULL,
pickup_latitude DECIMAL(10, 8) NOT NULL,
pickup_longitude DECIMAL(11, 8) NOT NULL,
pickup_address VARCHAR(255),
notified BOOLEAN DEFAULT FALSE,
notification_sent_at TIMESTAMP NULL,
call_made_at TIMESTAMP NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
INDEX idx_pnr_id (pnr_id),
INDEX idx_notified (notified)
```

---

## Technology Stack

- **Framework**: Spring Boot 3.2.5, Spring Kafka, Spring Data JPA
- **Database**: MySQL
- **External APIs**: Google Maps Distance Matrix API, Twilio (SMS/Voice)
- **Message Queue**: Apache Kafka
- **Java**: 17

---

## Kafka Integration

**Topic**: `bus-location-updates`

**Message Format**:
```json
{
  "bus_id": "BUS001",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Design**: No pnr_id list in message for scalability. System queries database.

---

## Project Structure

```
src/main/java/com/busreminder/
├── BusReminderApplication.java
├── config/
│   ├── KafkaConfig.java
│   ├── DatabaseConfig.java
│   └── DataLoader.java
├── consumer/
│   └── BusLocationConsumer.java
├── service/
│   ├── LocationProcessingService.java
│   ├── NotificationService.java
│   └── PassengerService.java
├── repository/
│   ├── BusPnrRepository.java
│   └── BusPassengerRepository.java
├── model/
│   ├── BusPnr.java
│   └── BusPassenger.java
└── dto/
    ├── BusLocationEvent.java
    └── NotificationRequest.java
```

---

## Key Design Decisions

1. **Event-Driven**: Kafka-based real-time processing
2. **PNR as Intermediate**: Industry-standard booking reference for flexible grouping
3. **No JPA Relationships**: Direct queries for simplicity and performance
4. **Lightweight Messages**: Minimal Kafka payload, query database for details
5. **Minimal Code**: Core functionality only, no over-engineering

---

## Performance Considerations

- Database connection pooling (HikariCP)
- Indexes on `bus_id` (bus_pnr) and `pnr_id` (bus_passenger)
- Kafka consumer groups for horizontal scaling
- Async processing for ETA calculations (future enhancement)
- Batch API calls to Maps API (future enhancement)
