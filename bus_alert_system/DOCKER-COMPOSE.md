# Docker Compose Setup for Kafka and Prometheus

This docker-compose file sets up Kafka (with Zookeeper) and Prometheus for the Bus Reminder System.

## Services

### Zookeeper
- **Port**: 2181
- Required for Kafka coordination

### Kafka
- **Ports**: 
  - 9092 (Kafka broker)
  - 9101 (JMX metrics)
- **Topics**: Auto-creation enabled
- Depends on Zookeeper

### Prometheus
- **Port**: 9090
- **Web UI**: http://localhost:9090
- Configured via `prometheus/prometheus.yml`

## Usage

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f kafka
docker-compose logs -f prometheus
```

### Access Prometheus UI
Open your browser and navigate to: http://localhost:9090

### Create Kafka Topic (optional)
```bash
docker exec -it kafka kafka-topics --create \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

### List Kafka Topics
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Produce Test Message
```bash
docker exec -it kafka kafka-console-producer \
  --topic bus-location-updates \
  --bootstrap-server localhost:9092
```

### Consume Messages
```bash
docker exec -it kafka kafka-console-consumer \
  --topic bus-location-updates \
  --from-beginning \
  --bootstrap-server localhost:9092
```

## Configuration

The Prometheus configuration can be found in `prometheus/prometheus.yml`. You can add additional scrape targets for:
- Spring Boot Actuator (if enabled)
- Kafka JMX metrics
- Other services

## Notes

- Kafka data is stored in the container (volatile, removed when container stops)
- Prometheus data is persisted in a named volume `prometheus-data`
- Kafka auto-creates topics when first message is sent (if topic doesn't exist)

## Send sample message
cat << 'EOF' | docker exec -i kafka kafka-console-producer --topic bus-location-updates --bootstrap-server localhost:9092
{"bus_id":"BUS001","latitude":40.7128,"longitude":-74.0060,"timestamp":"2024-01-15T10:30:00Z"}
{"bus_id":"BUS002","latitude":40.7580,"longitude":-73.9855,"timestamp":"2024-01-15T10:35:00Z"}
EOF

