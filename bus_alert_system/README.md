# Bus Reminder System

Spring Boot backend that processes real-time bus locations and sends arrival notifications to passengers via SMS and voice calls.

## Features

- Real-time bus location processing via Kafka
- ETA calculation (Google Maps API with Haversine fallback)
- SMS and voice notifications via Twilio
- Duplicate notification prevention
- REST API for testing
- 80%+ test coverage
- Docker & Kubernetes ready
- Swagger/OpenAPI documentation

## Tech Stack

- Spring Boot 3.2.5, Java 17, Maven
- MySQL 8.0+, Apache Kafka
- Google Maps API, Twilio

## Quick Start

```bash
# 1. Start dependencies
docker compose up -d

# 2. Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Test
curl http://localhost:8080/api/bus-location/health
```

## Documentation

- **[SETUP.md](SETUP.md)** - Complete setup guide (database, Kafka, API keys, Twilio)
- **[KAFKA.md](KAFKA.md)** - Kafka guide with CLI instructions for sending messages
- **[DATABASE_STRUCTURE.md](DATABASE_STRUCTURE.md)** - Database schema and table definitions
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Production deployment (Docker, Kubernetes, traditional)
- **[DEVELOPER.md](DEVELOPER.md)** - Development workflow, testing, code style
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design and components
- **[OPERATIONS.md](OPERATIONS.md)** - Operations, monitoring, troubleshooting

## API Endpoints

- `GET /api/bus-location/health` - Health check
- `POST /api/bus-location/update` - Update bus location

Swagger UI: http://localhost:8080/swagger-ui.html

## Project Structure

```
src/main/java/com/busreminder/
├── config/          # Configuration
├── controller/       # REST controllers
├── consumer/        # Kafka consumers
├── service/          # Business logic
├── repository/       # Data access
├── model/           # JPA entities
└── dto/             # Data transfer objects
```

## Configuration

Profiles: `local`, `dev`, `prod` (see `application-*.properties`)

## Testing

```bash
mvn test              # Run tests
mvn test jacoco:report # With coverage
```

Test coverage: **80%+**

## License

See [LICENSE](../LICENSE)
