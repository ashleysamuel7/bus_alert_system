# Setup Guide

Complete setup instructions for the Bus Reminder System.

## Prerequisites

- Java 17+, Maven 3.6+, Docker & Docker Compose
- MySQL 8.0+ (or use Docker)

## Quick Setup (5 minutes)

### 1. Start Dependencies

```bash
# Start Kafka & Zookeeper
docker compose up -d

# Start MySQL (if not running)
docker run --name mysql-bus-reminder \
  -e MYSQL_ROOT_PASSWORD=root1 \
  -e MYSQL_DATABASE=bus_reminder_db \
  -p 3306:3306 -d mysql:8.0
```

### 2. Configure Application

Edit `src/main/resources/application-local.properties`:
- Set MySQL password
- Add Google Maps API key (optional)
- Add Twilio credentials (optional)

### 3. Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. Verify

```bash
curl http://localhost:8080/api/bus-location/health
```

## Detailed Setup

### Database Setup

**Option 1: Docker (Recommended)**
```bash
docker run --name mysql-bus-reminder \
  -e MYSQL_ROOT_PASSWORD=root1 \
  -e MYSQL_DATABASE=bus_reminder_db \
  -p 3306:3306 -d mysql:8.0
```

**Option 2: Local MySQL**
```sql
CREATE DATABASE bus_reminder_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are auto-created on first run (JPA `ddl-auto=update`).

### Kafka Setup

**Using Docker Compose:**
```bash
docker compose up -d
# Starts: Zookeeper (2181), Kafka (9092), Prometheus (9090)
```

**Verify:**
```bash
docker compose ps
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable "Distance Matrix API"
3. Create API Key
4. Add to `application-local.properties`:
   ```properties
   google.maps.api.key=YOUR_API_KEY
   ```
**Note:** If not set, system uses Haversine formula (less accurate but works offline).

### Twilio Setup

1. Sign up at [Twilio](https://www.twilio.com/)
2. Get Account SID and Auth Token from Dashboard
3. Get a phone number
4. Create TwiML Bin for voice calls:
   - Go to Twilio Console → Runtime → TwiML Bins
   - Create new bin with:
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <Response>
         <Say voice="alice">Your bus will arrive in approximately 10 minutes. Please be ready at the pickup point.</Say>
     </Response>
     ```
   - Copy the TwiML URL

5. Add to `application-local.properties`:
   ```properties
   twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   twilio.auth.token=your_auth_token
   twilio.phone.number=+1234567890
   twilio.voice.url=https://handler.twilio.com/twiml/EHxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

**Note:** If not configured, notifications are skipped (logged as warnings).

### Data Loading

Enable sample data on startup:
```properties
data.loader.enabled=true
```
Loads: 10 buses, ~20-40 PNRs, 200 passengers

## Configuration Reference

### Database
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bus_reminder_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root1
spring.jpa.hibernate.ddl-auto=update
```

### Kafka
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=bus-location-consumer-group
kafka.topic.bus-location-updates=bus-location-updates
```

### Notification
```properties
notification.threshold.minutes=10  # Minutes before arrival to notify
```

## Secrets Management

**Never commit secrets to git!**

### Environment Variables (Recommended)

```bash
export DB_PASSWORD=your_password
export GOOGLE_MAPS_API_KEY=your_key
export TWILIO_ACCOUNT_SID=your_sid
```

Use in properties:
```properties
spring.datasource.password=${DB_PASSWORD}
google.maps.api.key=${GOOGLE_MAPS_API_KEY}
```

### .env File (Docker)

Create `.env` (add to `.gitignore`):
```env
DB_PASSWORD=your_password
GOOGLE_MAPS_API_KEY=your_key
TWILIO_ACCOUNT_SID=your_sid
```

### Production

Use secrets managers:
- AWS Secrets Manager
- HashiCorp Vault
- Kubernetes Secrets

## Troubleshooting

**Application won't start:**
- Check MySQL: `mysql -u root -p`
- Check Kafka: `docker compose ps`
- Check port 8080: `lsof -i :8080`

**No notifications:**
- Verify data loaded: Check database for passengers
- Check ETA calculation in logs
- Verify Twilio credentials

**Kafka connection errors:**
- Ensure services running: `docker compose ps`
- Test connection: `telnet localhost 9092`

## Next Steps

- See [DEPLOYMENT.md](DEPLOYMENT.md) for production setup
- See [DEVELOPER.md](DEVELOPER.md) for development workflow

