# Bus Passenger Reminder System - Architecture Outline

## Project Overview

This is a backend system built with Java Spring Boot to automate calls and notifications for bus passengers based on the current location of the bus and the estimated time to reach the passenger's pickup point. The system receives real-time bus location updates via Kafka, calculates arrival time using distance/time calculation services (e.g., Google Maps API), and automatically sends notifications and makes calls to passengers when the bus is approaching their pickup location.

**Scale Requirements:**
- Support 500 buses
- Support 25,000 passengers
- Bare minimal code to serve core functionality
- No seat booking management required

**Sample Data for Testing:**
- 10 buses with 20 passengers each (200 total passengers)

---

## System Architecture

### High-Level Architecture

```
┌─────────────┐
│   Kafka     │  Bus Location Events (bus_id, latitude, longitude, timestamp)
│             │  Note: No pnr_id list in message for scalability
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────┐
│         Spring Boot Application                 │
│  ┌──────────────────────────────────────────┐  │
│  │   Kafka Consumer Service                 │  │
│  │   - Consumes bus location events         │  │
│  └──────────────┬───────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼───────────────────────────┐  │
│  │   Location Processing Service            │  │
│  │   - Fetches pnr_ids for bus_id           │  │
│  │   - Fetches passengers for pnr_ids       │  │
│  │   - Calculates ETA using Maps API       │  │
│  │   - Determines when to notify/call      │  │
│  └──────────────┬───────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼───────────────────────────┐  │
│  │   Notification Service                   │  │
│  │   - Sends SMS via AWS SNS               │  │
│  │   - Makes phone calls                   │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
       │
       ▼
┌─────────────┐
│   MySQL DB  │  bus_pnr (bus_id -> pnr_id)
│             │  bus_passenger (pnr_id -> passengers)
└─────────────┘
       │
       ▼
┌─────────────┐
│ Google Maps │  Distance/Time Calculation API
│    API      │
└─────────────┘
       │
       ▼
┌─────────────┐
│   AWS SNS   │  SMS/Notification Service
└─────────────┘
```

---

## Core Components

### 1. Kafka Consumer Service
- **Responsibility**: Consume bus location events from Kafka topic
- **Input**: Kafka messages containing:
  - `bus_id`: Unique identifier for the bus
  - `latitude`: Current bus latitude
  - `longitude`: Current bus longitude
  - `timestamp`: Event timestamp
  - **Note**: Message does NOT include pnr_id list to keep JSON small and increase throughput
- **Output**: Triggers location processing for each bus update

### 2. Location Processing Service
- **Responsibility**: Process bus location and calculate ETAs
- **Key Functions**:
  - Query `bus_pnr` table to get all `pnr_id` for the given `bus_id` (one-to-many relationship)
  - Query `bus_passenger` table for all passengers associated with the `pnr_id`s
  - For each passenger pickup point, calculate ETA using Google Maps API (or alternative)
  - Determine if notification/call threshold is met (e.g., 10 minutes before arrival)
  - Track which passengers have already been notified to avoid duplicates

### 3. Notification Service
- **Responsibility**: Send notifications and make calls to passengers
- **Key Functions**:
  - Send SMS/notification messages via AWS SNS
  - Initiate automated phone calls
  - Handle notification delivery status
  - Prevent duplicate notifications
  - **Note**: Retry logic will be implemented only after testing the APIs

### 4. Database Service
- **Responsibility**: Data persistence and retrieval
- **Key Functions**:
  - Query `pnr_id`s by `bus_id` (one-to-many relationship)
  - Query passengers by `pnr_id`
  - Store passenger pickup locations
  - Track notification status

---

## Data Flow

1. **Bus Location Event Arrives**
   - Kafka consumer receives bus location event
   - Event contains: `bus_id`, `latitude`, `longitude`, `timestamp`
   - **Note**: Message does NOT include `pnr_id` list for scalability (shorter JSON, higher throughput)

2. **Fetch PNR IDs and Passengers**
   - Query `bus_pnr` table to get all `pnr_id`s for the given `bus_id` (one-to-many)
   - Query `bus_passenger` table for all passengers with matching `pnr_id`s
   - Retrieve passenger pickup locations (latitude, longitude)

3. **Calculate ETA**
   - For each passenger pickup point:
     - Call Google Maps Distance Matrix API (or alternative)
     - Calculate estimated time of arrival
     - Determine if notification threshold is met

4. **Send Notifications & Calls**
   - For passengers meeting threshold:
     - Send notification message
     - Initiate automated phone call
     - Mark as notified to prevent duplicates

---

## Technology Stack

### Core Framework
- **Java Spring Boot** (2.x or 3.x)
- **Spring Kafka** - Kafka consumer integration
- **Spring Data JPA** - Database access
- **MySQL** - Database (as per user preference)

### External Integrations
- **Google Maps API** (Distance Matrix API) - For ETA calculations
  - Alternative: OpenRouteService, Mapbox, or custom distance calculator
- **AWS SNS** - SMS/Notification Service
- **Voice Calling Service** - Twilio Voice API, AWS Connect, or similar

### Infrastructure
- **Apache Kafka** - Message broker for bus location events
- **MySQL Database** - Persistent storage

---

## Database Schema

### bus_pnr Table (Intermediate Table)
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

**Key Fields:**
- `bus_id`: Links to specific bus (one bus has many pnr_ids)
- `pnr_id`: Unique identifier acting as intermediate between bus and passengers
- **Relationship**: One bus_id → Many pnr_id (one-to-many)

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

**Key Fields:**
- `pnr_id`: Links passenger to PNR (which links to bus_id)
- `pickup_latitude`, `pickup_longitude`: Passenger pickup location
- `passenger_phone`: Phone number for calls/notifications
- `notified`: Flag to prevent duplicate notifications
- `notification_sent_at`, `call_made_at`: Timestamps for tracking

**Data Flow**: `bus_id` → `pnr_id` (via bus_pnr) → `passengers` (via bus_passenger)

---

## Kafka Integration

### Kafka Topic Structure
- **Topic Name**: `bus-location-updates` (configurable)
- **Message Format** (JSON):
```json
{
  "bus_id": "BUS001",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Consumer Configuration
- Consumer group for horizontal scaling
- Offset management (auto-commit or manual)
- Error handling and retry logic
- Batch processing for efficiency

### Design Decision: No pnr_id List in Kafka Message
- **Rationale**: Keep JSON messages small and lightweight
- **Benefit**: Increased throughput and better scalability
- **Trade-off**: System must query database to get pnr_ids for each bus_id
- **Impact**: Minimal - database lookup is fast with proper indexing

---

## API Endpoints (Minimal)

Since this is a backend service focused on processing Kafka events, minimal REST endpoints may be needed:

### Optional Endpoints (if required)
- `GET /health` - Health check endpoint
- `GET /metrics` - System metrics (if needed)
- `POST /passenger` - Register passenger (if not handled elsewhere)
- `GET /passenger/{bus_id}` - Get passengers for a bus (for debugging)

---

## External Service Integration

### Google Maps Distance Matrix API
- **Purpose**: Calculate travel time from bus location to passenger pickup point
- **Usage**: 
  - Input: Origin (bus location), Destination (passenger pickup)
  - Output: Duration in seconds/minutes
- **Rate Limits**: Consider caching and batch requests
- **Alternative**: OpenRouteService, Mapbox Matrix API

### Notification Service
- **SMS/Notification**: **AWS SNS** (Amazon Simple Notification Service)
- **Voice Calls**: Twilio Voice API, AWS Connect, or similar
- **Message Template**: "Your bus will arrive at [pickup_location] in approximately [X] minutes."
- **Retry Logic**: Will be implemented only after testing the APIs

---

## Scalability Considerations

### For 500 Buses & 25,000 Passengers
- **Average**: ~50 passengers per bus
- **Processing Strategy**:
  - Async processing for ETA calculations
  - Batch API calls to Maps API where possible
  - Connection pooling for database
  - Kafka consumer groups for horizontal scaling
  - Caching frequently accessed data (bus_id → pnr_id mappings)
  - Lightweight Kafka messages (no pnr_id list) for higher throughput

### Performance Optimizations
- **Database Indexing**: 
  - Index on `bus_id` in `bus_pnr` table for fast pnr_id lookups
  - Index on `pnr_id` in `bus_passenger` table for fast passenger lookups
- **API Rate Limiting**: Respect Google Maps API rate limits
- **Async Processing**: Use `@Async` for non-blocking operations
- **Connection Pooling**: Configure HikariCP for database connections
- **Caching**: Cache bus_id → pnr_id mappings to reduce DB queries
- **Lightweight Kafka Messages**: No pnr_id list in messages for better throughput

---

## Project Structure (Minimal)

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

1. **Event-Driven Architecture**: Kafka-based for real-time processing
2. **Minimal REST API**: Focus on Kafka consumer, REST only if needed
3. **PNR as Intermediate Entity**: 
   - `bus_id` → `pnr_id` (one-to-many) → `passengers`
   - Allows flexible grouping of passengers under PNRs
   - Kafka messages don't include pnr_id list for scalability
4. **External API Integration**: 
   - Google Maps for ETA calculations
   - AWS SNS for SMS/notifications
5. **Database**: MySQL with simple schema: `bus_pnr` and `bus_passenger` tables
6. **No Seat Booking**: System only handles notifications, not seat management
7. **Bare Minimal Code**: Focus on core functionality without over-engineering
8. **Retry Logic**: Will be implemented only after API testing is complete

---

## Next Steps

1. Set up Spring Boot project with dependencies
2. Configure Kafka consumer
3. Create database schema (bus_pnr and bus_passenger tables) and JPA entities
4. Populate sample data: 10 buses with 20 passengers each (200 total passengers)
5. Implement location processing service with pnr_id lookup
6. Integrate Google Maps API
7. Implement AWS SNS notification service
8. Test APIs and then implement retry logic
9. Add error handling and logging
10. Scale testing: 500 buses with 25,000 passengers
11. Configure for production deployment

