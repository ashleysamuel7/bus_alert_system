# Kafka Guide

Complete guide for working with Kafka in the Bus Reminder System.

## Overview

The system uses Apache Kafka to receive real-time bus location updates. Messages are consumed from the `bus-location-updates` topic.

## Setup

### Start Kafka with Docker Compose

```bash
cd bus_alert_system
docker compose up -d
```

This starts:
- **Zookeeper** (port 2181)
- **Kafka** (port 9092)
- **Prometheus** (port 9090, optional)

### Verify Services

```bash
docker compose ps
```

Should show all services as "Up".

## Topic Configuration

- **Topic Name:** `bus-location-updates`
- **Auto-creation:** Enabled (topic created automatically on first message)
- **Consumer Group:** `bus-location-consumer-group`

## Sending Messages via CLI

### Using Docker Exec (Recommended)

**Send a single message:**
```bash
docker exec -i kafka kafka-console-producer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

Then type your JSON message and press Enter:
```json
{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}
```

Press `Ctrl+C` to exit.

**Send message from file:**
```bash
echo '{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}' | \
  docker exec -i kafka kafka-console-producer.sh \
    --topic bus-location-updates \
    --bootstrap-server localhost:9092
```

**Send multiple messages:**
```bash
cat << 'EOF' | docker exec -i kafka kafka-console-producer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}
{"bus_id":"BUS002","latitude":40.7580,"longitude":-73.9855,"timestamp":"2024-01-15T10:35:00Z"}
{"bus_id":"BUS003","latitude":40.7614,"longitude":-73.9776,"timestamp":"2024-01-15T10:40:00Z"}
EOF
```

### Message Format

Messages must be valid JSON with the following structure:

```json
{
  "bus_id": "BUS001",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Fields:**
- `bus_id` (required): Bus identifier (e.g., "BUS001")
- `latitude` (required): Bus latitude (-90 to 90)
- `longitude` (required): Bus longitude (-180 to 180)
- `timestamp` (required): ISO 8601 timestamp (e.g., "2024-01-15T10:30:00Z")

### Example Messages

**New York City area:**
```json
{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}
```

**Different bus:**
```json
{"bus_id":"BUS002","latitude":40.7580,"longitude":-73.9855,"timestamp":"2024-01-15T11:00:00Z"}
```

**Current timestamp:**
```json
{"bus_id":"BUS003","latitude":40.7614,"longitude":-73.9776,"timestamp":"2024-01-15T12:00:00Z"}
```

## Consuming Messages

### View Messages in Real-Time

```bash
docker exec -it kafka kafka-console-consumer.sh \
  --topic bus-location-updates \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### View Latest Messages Only

```bash
docker exec -it kafka kafka-console-consumer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

### Consume with Consumer Group

```bash
docker exec -it kafka kafka-console-consumer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092 \
  --group test-consumer-group \
  --from-beginning
```

## Topic Management

### List All Topics

```bash
docker exec -it kafka kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092
```

### Describe Topic

```bash
docker exec -it kafka kafka-topics.sh \
  --describe \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

### Create Topic Manually (Optional)

```bash
docker exec -it kafka kafka-topics.sh \
  --create \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

### Delete Topic

```bash
docker exec -it kafka kafka-topics.sh \
  --delete \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

## Consumer Group Management

### List Consumer Groups

```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### Describe Consumer Group

```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group bus-location-consumer-group \
  --describe
```

This shows:
- Partition assignments
- Current offsets
- Consumer lag

### Reset Consumer Group Offset

**Reset to earliest:**
```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group bus-location-consumer-group \
  --reset-offsets \
  --to-earliest \
  --topic bus-location-updates \
  --execute
```

**Reset to latest:**
```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group bus-location-consumer-group \
  --reset-offsets \
  --to-latest \
  --topic bus-location-updates \
  --execute
```

## Testing Workflow

### 1. Start Kafka

```bash
docker compose up -d
```

### 2. Verify Topic Exists (or create it)

```bash
docker exec -it kafka kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092
```

### 3. Start Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. Send Test Message

```bash
echo '{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}' | \
  docker exec -i kafka kafka-console-producer.sh \
    --topic bus-location-updates \
    --bootstrap-server localhost:9092
```

### 5. Check Application Logs

Look for:
- `Received bus location event`
- `Calculated ETA`
- `SMS sent` or `Call initiated`

## Troubleshooting

### Kafka Not Running

```bash
# Check status
docker compose ps

# Check logs
docker compose logs kafka

# Restart
docker compose restart kafka
```

### Cannot Connect to Kafka

```bash
# Test connection
telnet localhost 9092

# Or
nc -zv localhost 9092

# Check if port is in use
lsof -i :9092
```

### Messages Not Being Consumed

1. **Check consumer group:**
   ```bash
   docker exec -it kafka kafka-consumer-groups.sh \
     --bootstrap-server localhost:9092 \
     --group bus-location-consumer-group \
     --describe
   ```

2. **Check application logs** for errors

3. **Verify topic exists:**
   ```bash
   docker exec -it kafka kafka-topics.sh \
     --list \
     --bootstrap-server localhost:9092
   ```

4. **Check message format** - must be valid JSON

### Consumer Lag

If consumer lag is high:
```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group bus-location-consumer-group \
  --describe
```

**Solutions:**
- Increase consumer concurrency in application
- Scale application horizontally
- Check application performance

### Invalid Message Format

Ensure messages are valid JSON:
```bash
# Test JSON validity
echo '{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}' | jq .
```

## Advanced Usage

### Send Messages with Key

```bash
docker exec -i kafka kafka-console-producer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092 \
  --property "parse.key=true" \
  --property "key.separator=:"
```

Then send: `BUS001:{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}`

### View Messages with Timestamps

```bash
docker exec -it kafka kafka-console-consumer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092 \
  --property print.timestamp=true \
  --from-beginning
```

### Monitor Topic Throughput

```bash
# In one terminal - produce messages
while true; do
  echo '{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"}' | \
    docker exec -i kafka kafka-console-producer.sh \
      --topic bus-location-updates \
      --bootstrap-server localhost:9092
  sleep 1
done

# In another terminal - consume messages
docker exec -it kafka kafka-console-consumer.sh \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

## Integration with Application

The Spring Boot application automatically:
- Connects to Kafka on startup
- Subscribes to `bus-location-updates` topic
- Processes messages as they arrive
- Logs processing details

**Configuration** (in `application-local.properties`):
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=bus-location-consumer-group
kafka.topic.bus-location-updates=bus-location-updates
```

## Additional Resources

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka Console Producer](https://kafka.apache.org/documentation/#basic_ops_console_producer)
- [Kafka Console Consumer](https://kafka.apache.org/documentation/#basic_ops_console_consumer)
- [Application SETUP.md](SETUP.md) - Full setup guide

