# Bus Passenger Reminder System - Architecture Outline

## Project Overview

Backend system built with Java Spring Boot to automate calls and notifications for bus passengers based on real-time bus location. The system receives bus location updates via Kafka, calculates ETA using Google Maps API, and sends notifications/calls when the bus approaches passenger pickup points.

**Scale Requirements:**
- 500 buses, 25,000 passengers
- Bare minimal code for core functionality
- No seat booking management

**Sample Data:** 10 buses with 20 passengers each (200 total)

---

## Understanding PNR (Passenger Name Record)

**PNR** is an industry-standard booking reference that groups passengers traveling together (originally from airlines, now used across transportation). In this system:

- **Purpose**: Groups passengers by booking transaction (`bus_id` → `pnr_id` → `passengers`)
- **Structure**: One bus can have multiple PNRs (different bookings), one PNR can have multiple passengers (traveling together)
- **Benefits**: Enables batch operations, booking-level management, efficient querying, and supports shared pickup points

**Example**: Bus BUS001 has 3 PNRs:
- PNR001: 4 passengers (family booking, shared pickup)
- PNR002: 2 passengers (couple booking)
- PNR003: 1 passenger (individual booking)

---

## System Architecture

```
Kafka (bus_id, lat, lng, timestamp)
    ↓
Spring Boot App
    ├─ Kafka Consumer → Location Processing Service
    │                      ├─ Query bus_pnr (get pnr_ids)
    │                      ├─ Query bus_passenger (get passengers)
    │                      └─ Calculate ETA (Google Maps API)
    └─ Notification Service (AWS SNS)
    ↓
MySQL (bus_pnr, bus_passenger)
```

---

## Core Components

1. **Kafka Consumer Service**: Consumes bus location events (bus_id, latitude, longitude, timestamp)
2. **Location Processing Service**: 
   - Fetches pnr_ids for bus_id → fetches passengers for pnr_ids
   - Calculates ETA using Google Maps API
   - Determines notification threshold (e.g., 10 min before arrival)
3. **Notification Service**: Sends SMS via AWS SNS, makes phone calls, prevents duplicates
4. **Database Service**: Queries by bus_id → pnr_id → passengers, tracks notification status

---

## Data Flow

1. **Kafka Event**: Receives `bus_id`, `latitude`, `longitude`, `timestamp` (no pnr_id list for scalability)
2. **Fetch Data**: Query `bus_pnr` for pnr_ids → Query `bus_passenger` for passengers
3. **Calculate ETA**: Call Google Maps API for each passenger pickup point
4. **Notify**: Send SMS/calls to passengers meeting threshold, mark as notified

---

## Technology Stack

- **Framework**: Java Spring Boot, Spring Kafka, Spring Data JPA
- **Database**: MySQL
- **External APIs**: Google Maps Distance Matrix API, AWS SNS
- **Infrastructure**: Apache Kafka, MySQL

---

## Database Schema

### bus_pnr Table
```sql
CREATE TABLE bus_pnr (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bus_id VARCHAR(50) NOT NULL,
    pnr_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bus_id (bus_id),
    INDEX idx_pnr_id (pnr_id),
    UNIQUE KEY uk_bus_pnr (bus_id, pnr_id)
);
```

### bus_passenger Table
```sql
CREATE TABLE bus_passenger (
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
);
```

**Relationship**: `bus_id` → `pnr_id` (one-to-many) → `passengers` (one-to-many)

---

## Entity Design (JPA)

### BusPnr Entity
- Fields: `id`, `busId`, `pnrId`, `createdAt`
- Unique constraint: `busId` + `pnrId`
- No JPA relationships (minimal design, direct queries)

### BusPassenger Entity
- Fields: `id`, `pnrId`, `passengerId`, `passengerName`, `passengerPhone`, `pickupLatitude`, `pickupLongitude`, `pickupAddress`, `notified`, `notificationSentAt`, `callMadeAt`, `createdAt`
- Indexes on `pnrId` and `notified`

**Query Pattern:**
```java
List<BusPnr> pnrs = busPnrRepository.findByBusId(busId);
List<String> pnrIds = pnrs.stream().map(BusPnr::getPnrId).collect(Collectors.toList());
List<BusPassenger> passengers = busPassengerRepository.findByPnrIdIn(pnrIds);
```

---

## Kafka Integration

**Topic**: `bus-location-updates`

**Message Format:**
```json
{
  "bus_id": "BUS001",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Design Decision**: No pnr_id list in message to keep JSON lightweight and increase throughput. System queries database for pnr_ids.

---

## External Service Integration

- **Google Maps Distance Matrix API**: Calculate travel time (origin: bus location, destination: passenger pickup)
- **AWS SNS**: SMS/notification service
- **Voice Calls**: Twilio Voice API, AWS Connect, or similar
- **Retry Logic**: Implemented after API testing

---

## Scalability & Performance

**For 500 buses & 25,000 passengers (~50 passengers/bus):**
- Async processing for ETA calculations
- Batch API calls to Maps API
- Database connection pooling (HikariCP)
- Kafka consumer groups for horizontal scaling
- Caching bus_id → pnr_id mappings
- Indexes on `bus_id` (bus_pnr) and `pnr_id` (bus_passenger)

---

## Project Structure

```
src/main/java/com/busreminder/
├── BusReminderApplication.java
├── config/
│   ├── KafkaConfig.java
│   └── DatabaseConfig.java
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
2. **Minimal REST API**: Focus on Kafka consumer
3. **PNR as Intermediate**: Industry-standard booking reference enabling flexible grouping and batch operations
4. **No JPA Relationships**: Direct queries for simplicity and performance
5. **Lightweight Kafka Messages**: No pnr_id list for scalability
6. **Bare Minimum Code**: Core functionality only, no over-engineering

---

## Next Steps

1. Set up Spring Boot project with dependencies
2. Configure Kafka consumer
3. Create database schema and JPA entities
4. Populate sample data (10 buses, 20 passengers each)
5. Implement location processing service
6. Integrate Google Maps API
7. Implement AWS SNS notification service
8. Test APIs and implement retry logic
9. Add error handling and logging
10. Scale testing (500 buses, 25,000 passengers)
11. Production deployment
