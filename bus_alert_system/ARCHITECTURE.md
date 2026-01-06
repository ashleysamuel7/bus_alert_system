# Architecture

System architecture and design for the Bus Reminder System.

## System Overview

Event-driven microservice that processes real-time bus location updates and sends notifications to passengers when buses approach pickup points.

## Architecture Diagram

```
Bus Tracking System
    │
    │ Publishes events
    ▼
┌─────────────────────┐
│   Apache Kafka      │
│ bus-location-updates│
└──────────┬──────────┘
           │
           │ Consumes
           ▼
┌──────────────────────────────┐
│  Spring Boot Application     │
│  ┌────────────────────────┐  │
│  │ BusLocationConsumer    │  │
│  └───────────┬────────────┘  │
│              │               │
│              ▼               │
│  ┌────────────────────────┐  │
│  │LocationProcessingService│ │
│  │  ├─ PassengerService   │  │
│  │  └─ calculateETA()      │  │
│  │     ├─ Google Maps API │  │
│  │     └─ Haversine        │  │
│  └───────────┬────────────┘  │
│              │               │
│              ▼               │
│  ┌────────────────────────┐  │
│  │ NotificationService    │  │
│  │  ├─ sendSMS() (Twilio) │  │
│  │  └─ makeCall() (Twilio)│  │
│  └────────────────────────┘  │
└───────────┬──────────────────┘
            │
            │ Queries & Updates
            ▼
┌──────────────────────────┐
│      MySQL Database      │
│  ┌──────┐  ┌──────────┐ │
│  │bus_pnr│  │bus_passenger││
│  └──────┘  └──────────┘ │
└──────────────────────────┘
```

## Core Components

### 1. BusLocationConsumer

- Consumes Kafka messages from `bus-location-updates` topic
- Deserializes JSON to `BusLocationEvent`
- Delegates to `LocationProcessingService`

### 2. LocationProcessingService

- Fetches passengers for bus_id
- Calculates ETA (Google Maps API or Haversine fallback)
- Filters by notification threshold (default: 10 minutes)

### 3. NotificationService

- Sends SMS and voice calls via Twilio
- Marks passengers as notified
- Handles missing configuration gracefully

### 4. PassengerService

- Database queries for passenger data
- Query pattern: `bus_id → bus_pnr → bus_passenger`

## Data Flow

1. **Event Reception:** Kafka message received and deserialized
2. **Passenger Lookup:** Query database for passengers on bus
3. **ETA Calculation:** Calculate ETA for each passenger (Google Maps or Haversine)
4. **Notification:** Send SMS/call if ETA ≤ threshold, mark as notified

## Error Handling

- **Kafka Errors:** Log and continue, don't commit offset
- **Google Maps API:** Fallback to Haversine formula
- **Twilio Errors:** Log and continue with other passengers
- **Database Errors:** Log and return empty list

## Scalability

- **Horizontal Scaling:** Multiple instances with same consumer group
- **Kafka:** Automatic partition assignment, load distribution
- **Database:** Connection pooling, query optimization, indexes

## Technology Decisions

- **Spring Boot:** Rapid development, auto-configuration, production-ready
- **Kafka:** High throughput, durability, scalability, decoupling
- **MySQL:** ACID compliance, mature ecosystem, good performance
- **Twilio:** Reliable SMS/Voice, easy integration, global coverage

## Future Enhancements

- Async processing for ETA calculations
- Caching (bus_id → pnr_id mappings, ETA results)
- Batch processing (Google Maps API calls, database updates)
- Circuit breakers and retry mechanisms
- Prometheus metrics and distributed tracing
