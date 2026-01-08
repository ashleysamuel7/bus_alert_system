# Database Structure

Complete database schema documentation for the Bus Reminder System.

## Overview

The system uses MySQL database with two main tables:
- `bus_pnr` - Links buses to PNR (Passenger Name Record) IDs
- `bus_passenger` - Stores passenger information and notification status

## Database: `bus_reminder_db`

## Tables

### 1. `bus_pnr`

Links bus identifiers to PNR (Passenger Name Record) IDs. This table acts as a junction table connecting buses with passenger groups.

#### Schema

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `bus_id` | VARCHAR(50) | NOT NULL | Bus identifier (e.g., "BUS001") |
| `pnr_id` | VARCHAR(50) | NOT NULL | PNR (Passenger Name Record) identifier |
| `created_at` | TIMESTAMP | NULLABLE | Record creation timestamp |

#### Constraints

- **Primary Key:** `id`
- **Unique Constraint:** (`bus_id`, `pnr_id`) - A bus can have multiple PNRs, but each bus-PNR combination is unique
- **Auto-populated:** `id` (auto-increment), `created_at` (on insert via @PrePersist)

#### Indexes

- Index on `bus_id` (for fast bus lookup)
- Unique index on (`bus_id`, `pnr_id`)

#### Example Data

```sql
INSERT INTO bus_pnr (bus_id, pnr_id) VALUES
('BUS001', 'PNR001'),
('BUS001', 'PNR002'),
('BUS002', 'PNR003');
```

#### Relationships

- **Many-to-Many with `bus_passenger`**: One bus can have multiple PNRs, one PNR can have multiple passengers
- Query pattern: `bus_id` → `pnr_id` → `bus_passenger` (via `pnr_id`)

---

### 2. `bus_passenger`

Stores passenger information, pickup locations, and notification tracking status.

#### Schema

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `pnr_id` | VARCHAR(50) | NOT NULL | PNR identifier (links to bus_pnr) |
| `passenger_id` | VARCHAR(50) | NOT NULL | Unique passenger identifier |
| `passenger_name` | VARCHAR(100) | NULLABLE | Passenger name |
| `passenger_phone` | VARCHAR(20) | NOT NULL | Passenger phone number (for SMS/calls) |
| `pickup_latitude` | DECIMAL(10,8) | NOT NULL | Pickup location latitude (-90 to 90) |
| `pickup_longitude` | DECIMAL(11,8) | NOT NULL | Pickup location longitude (-180 to 180) |
| `pickup_address` | VARCHAR(255) | NULLABLE | Human-readable pickup address |
| `notified` | BOOLEAN | NOT NULL, DEFAULT FALSE | Whether notification has been sent |
| `notification_sent_at` | TIMESTAMP | NULLABLE | When notification was sent |
| `call_made_at` | TIMESTAMP | NULLABLE | When voice call was made |
| `created_at` | TIMESTAMP | NULLABLE | Record creation timestamp |

#### Constraints

- **Primary Key:** `id`
- **Auto-populated:** 
  - `id` (auto-increment)
  - `created_at` (on insert via @PrePersist)
  - `notified` (defaults to `false` if null)

#### Indexes

- Index on `pnr_id` (for fast PNR lookup)
- Index on `passenger_id` (for passenger lookup)
- Index on `notified` (for filtering unnotified passengers)

#### Example Data

```sql
INSERT INTO bus_passenger (
    pnr_id,
    passenger_id,
    passenger_name,
    passenger_phone,
    pickup_latitude,
    pickup_longitude,
    pickup_address,
    notified
) VALUES (
    'PNR001',
    'PASS001',
    'John Doe',
    '+1234567890',
    40.7128,
    -74.0060,
    '123 Main St, New York, NY',
    false
);
```

#### Relationships

- **Many-to-One with `bus_pnr`**: Multiple passengers can share the same PNR ID
- Query pattern: `bus_id` → `bus_pnr.pnr_id` → `bus_passenger.pnr_id`

---

## Entity Relationship Diagram

```
┌─────────────────┐
│    bus_pnr      │
├─────────────────┤
│ id (PK)         │
│ bus_id          │──┐
│ pnr_id          │  │
│ created_at      │  │
└─────────────────┘  │
                     │
                     │ Many-to-Many
                     │ via pnr_id
                     │
┌─────────────────┐  │
│ bus_passenger   │  │
├─────────────────┤  │
│ id (PK)         │  │
│ pnr_id          │──┘
│ passenger_id    │
│ passenger_name  │
│ passenger_phone │
│ pickup_latitude │
│ pickup_longitude│
│ pickup_address  │
│ notified        │
│ notification_   │
│   sent_at       │
│ call_made_at    │
│ created_at      │
└─────────────────┘
```

## Data Flow

### Finding Passengers for a Bus

1. **Query `bus_pnr`** by `bus_id` to get all associated `pnr_id`s
2. **Query `bus_passenger`** by `pnr_id` IN (list from step 1)
3. **Filter** by `notified = false` to get passengers who haven't been notified

### SQL Query Example

```sql
-- Get all unnotified passengers for BUS001
SELECT bp.*
FROM bus_passenger bp
WHERE bp.pnr_id IN (
    SELECT pnr_id 
    FROM bus_pnr 
    WHERE bus_id = 'BUS001'
)
AND bp.notified = false;
```

## Common Operations

### Insert a Bus-PNR Link

```sql
INSERT INTO bus_pnr (bus_id, pnr_id) 
VALUES ('BUS001', 'PNR001');
```

### Insert a Passenger

```sql
INSERT INTO bus_passenger (
    pnr_id,
    passenger_id,
    passenger_name,
    passenger_phone,
    pickup_latitude,
    pickup_longitude,
    pickup_address,
    notified
) VALUES (
    'PNR001',
    'PASS001',
    'John Doe',
    '+1234567890',
    40.7128,
    -74.0060,
    '123 Main St, New York, NY',
    false
);
```

### Mark Passenger as Notified

```sql
UPDATE bus_passenger 
SET 
    notified = true,
    notification_sent_at = NOW(),
    call_made_at = NOW()
WHERE passenger_id = 'PASS001';
```

### Get Passengers by Bus ID

```sql
SELECT bp.*
FROM bus_passenger bp
INNER JOIN bus_pnr bpnr ON bp.pnr_id = bpnr.pnr_id
WHERE bpnr.bus_id = 'BUS001'
AND bp.notified = false;
```

### Reset Notifications (for testing)

```sql
UPDATE bus_passenger 
SET 
    notified = false,
    notification_sent_at = NULL,
    call_made_at = NULL;
```

## Table Creation

Tables are automatically created by JPA/Hibernate on application startup when:
- `spring.jpa.hibernate.ddl-auto=update` (development)
- `spring.jpa.hibernate.ddl-auto=create` (testing)

For production, use:
- `spring.jpa.hibernate.ddl-auto=validate` (validate schema only)
- Or use Flyway/Liquibase for version-controlled migrations

## Notes

- **Coordinates**: 
  - Latitude: -90 to 90 (DECIMAL(10,8))
  - Longitude: -180 to 180 (DECIMAL(11,8))
  
- **Phone Numbers**: Should include country code (e.g., `+1234567890`)

- **PNR ID**: Acts as a grouping mechanism - passengers with the same PNR are typically traveling together

- **Notification Tracking**: 
  - `notified` flag prevents duplicate notifications
  - Timestamps track when notifications were sent

- **Auto-population**: 
  - `id` is auto-generated
  - `created_at` is set via @PrePersist
  - `notified` defaults to `false` if not specified

