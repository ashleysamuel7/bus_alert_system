# Deployment Guide

Production deployment guide for the Bus Reminder System.

## Prerequisites

- Java 17+, MySQL 8.0+, Kafka 2.8+
- Docker 20.10+ (containerized) or Kubernetes 1.20+ (K8s)

## Resource Requirements

**Minimum:** 1 CPU, 2GB RAM, 10GB disk  
**Recommended:** 2-4 CPU, 4-8GB RAM, 50GB disk

## Docker Deployment

### 1. Build Image

```bash
mvn clean package -DskipTests
docker build -t bus-reminder-system:1.0.0 .
```

### 2. Deploy with Docker Compose

Create `docker-compose.prod.yml`:

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: bus_reminder_db
    volumes:
      - mysql-data:/var/lib/mysql

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

  app:
    image: bus-reminder-system:1.0.0
    depends_on:
      - mysql
      - kafka
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_PASSWORD: ${DB_PASSWORD}
      GOOGLE_MAPS_API_KEY: ${GOOGLE_MAPS_API_KEY}
      TWILIO_ACCOUNT_SID: ${TWILIO_ACCOUNT_SID}
      TWILIO_AUTH_TOKEN: ${TWILIO_AUTH_TOKEN}
    restart: unless-stopped
```

### 3. Deploy

```bash
# Create .env with secrets
cat > .env << EOF
DB_PASSWORD=your_secure_password
GOOGLE_MAPS_API_KEY=your_key
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
EOF

docker compose -f docker-compose.prod.yml up -d
```

## Kubernetes Deployment

### 1. Create Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: bus-reminder
```

### 2. Create Secrets

```bash
kubectl create secret generic bus-reminder-secrets \
  --from-literal=db-password=your_password \
  --from-literal=google-maps-api-key=your_key \
  --from-literal=twilio-account-sid=your_sid \
  --from-literal=twilio-auth-token=your_token \
  -n bus-reminder
```

### 3. Create Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bus-reminder-app
  namespace: bus-reminder
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bus-reminder
  template:
    spec:
      containers:
      - name: app
        image: bus-reminder-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bus-reminder-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /api/bus-location/health
            port: 8080
          initialDelaySeconds: 60
        readinessProbe:
          httpGet:
            path: /api/bus-location/health
            port: 8080
          initialDelaySeconds: 30
```

### 4. Deploy

```bash
kubectl apply -f k8s/
kubectl get pods -n bus-reminder
```

## Traditional Server Deployment

### 1. Build

```bash
mvn clean package -DskipTests
```

### 2. Create Service User

```bash
sudo useradd -r -s /bin/false busreminder
sudo mkdir -p /opt/bus-reminder
```

### 3. Deploy

```bash
sudo cp target/bus-alert-system-1.0.0.jar /opt/bus-reminder/
sudo chown busreminder:busreminder /opt/bus-reminder/*.jar
```

### 4. Create Systemd Service

`/etc/systemd/system/bus-reminder.service`:

```ini
[Unit]
Description=Bus Reminder System
After=network.target mysql.service

[Service]
Type=simple
User=busreminder
WorkingDirectory=/opt/bus-reminder
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_PASSWORD=your_password"
ExecStart=/usr/bin/java -jar /opt/bus-reminder/bus-alert-system-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

### 5. Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable bus-reminder
sudo systemctl start bus-reminder
```

## Environment Configuration

### Production Properties

```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-db:3306/bus_reminder_db?useSSL=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.kafka.bootstrap-servers=prod-kafka-1:9092,prod-kafka-2:9092
spring.jpa.hibernate.ddl-auto=validate
data.loader.enabled=false
```

### Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_PASSWORD=secure_password
export GOOGLE_MAPS_API_KEY=prod_key
export TWILIO_ACCOUNT_SID=prod_sid
export TWILIO_AUTH_TOKEN=prod_token
```

## Database Migration

**Development:** `spring.jpa.hibernate.ddl-auto=update` (auto-creates)  
**Production:** `spring.jpa.hibernate.ddl-auto=validate` (manual migrations)

For production, use Flyway or Liquibase for version-controlled migrations.

## Health Checks

```bash
curl http://localhost:8080/api/bus-location/health
```

Expected: `{"status":"UP","service":"Bus Reminder System"}`

## Monitoring

- **Logs:** `docker logs bus-reminder-app` or `kubectl logs deployment/bus-reminder-app`
- **Metrics:** Prometheus (included in docker-compose)
- **Key Metrics:** Request rate, error rate, Kafka consumer lag, notification success rate

## Rollback

**Docker:**
```bash
docker compose -f docker-compose.prod.yml down
docker tag bus-reminder-system:previous-version bus-reminder-system:1.0.0
docker compose -f docker-compose.prod.yml up -d
```

**Kubernetes:**
```bash
kubectl rollout undo deployment/bus-reminder-app -n bus-reminder
```

**Systemd:**
```bash
sudo systemctl stop bus-reminder
sudo cp /opt/bus-reminder/backup/previous.jar /opt/bus-reminder/bus-alert-system-1.0.0.jar
sudo systemctl start bus-reminder
```

## Post-Deployment Verification

1. Health check: `curl http://your-server:8080/api/bus-location/health`
2. Test bus location update
3. Check logs for errors
4. Monitor metrics

## Troubleshooting

- **Won't start:** Check Java version, database/Kafka connectivity, logs
- **High memory:** Adjust JVM heap: `-Xmx4g -Xms2g`
- **Kafka lag:** Increase consumer concurrency or scale horizontally
- **DB connections:** Adjust connection pool settings

