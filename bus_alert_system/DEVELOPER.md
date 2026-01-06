# Developer Guide

Development workflow and guidelines for the Bus Reminder System.

## Getting Started

```bash
# Clone and setup
git clone <repository-url>
cd bus_alert_system

# Install dependencies
mvn clean install

# Start dependencies
docker compose up -d

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Project Structure

```
src/main/java/com/busreminder/
├── BusReminderApplication.java    # Main class
├── config/                         # Configuration
├── controller/                     # REST controllers
├── consumer/                       # Kafka consumers
├── service/                        # Business logic
├── repository/                     # Data access
├── model/                          # JPA entities
└── dto/                            # Data transfer objects
```

## Development Workflow

### Branch Strategy

- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

### Creating a Feature

```bash
git checkout -b feature/your-feature-name
# Make changes
mvn test
git commit -m "feat: description"
git push origin feature/your-feature-name
```

### Commit Messages

Follow conventional commits:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation
- `test:` - Tests
- `refactor:` - Code refactoring

## Testing

### Running Tests

```bash
mvn test                           # All tests
mvn test -Dtest=NotificationServiceTest  # Specific test
mvn test jacoco:report             # With coverage
```

### Writing Tests

**Unit Tests:**
```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private YourService service;
    
    @Test
    void testMethodName_Scenario_ExpectedResult() {
        // Given, When, Then
    }
}
```

**Coverage:** Maintain 80%+ coverage

## Code Style

### Java Conventions

- Follow Java naming conventions
- Keep methods small (< 50 lines)
- Add JavaDoc for public methods
- Use `@Override` annotation

### Spring Boot Conventions

- Use `@Service` for business logic
- Use `@Repository` for data access
- Inject dependencies via constructor
- Use `@Value` for configuration

### Example

```java
@Service
public class ExampleService {
    private static final Logger logger = LoggerFactory.getLogger(ExampleService.class);
    private final Dependency dependency;
    
    public ExampleService(Dependency dependency) {
        this.dependency = dependency;
    }
    
    public Result process(Input input) {
        logger.debug("Processing input: {}", input);
        return result;
    }
}
```

## Debugging

### IntelliJ IDEA / VS Code

1. Set breakpoints
2. Run → Debug 'BusReminderApplication'
3. Use debugger controls

### Command Line

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

Attach debugger to port 5005.

### Logging

**Log Levels:**
- `ERROR` - Errors requiring attention
- `WARN` - Warnings
- `INFO` - Important business events
- `DEBUG` - Detailed debugging

**Example:**
```java
logger.info("Processing bus location: busId={}, lat={}, lng={}", busId, lat, lng);
logger.error("Error processing event: {}", e.getMessage(), e);
```

## Common Tasks

### Adding a New Service

1. Create service class with `@Service`
2. Write tests
3. Inject where needed

### Adding a New Endpoint

1. Add method to controller:
```java
@PostMapping("/new-endpoint")
public ResponseEntity<?> newEndpoint(@RequestBody Request request) {
    // Implementation
}
```
2. Add Swagger annotations
3. Write tests
4. Update Postman collection

### Modifying Database Schema

1. Update entity class
2. JPA auto-updates (development)
3. Create migration script (production)
4. Update tests

## API Testing with Postman

### Import Collection

1. Open Postman
2. Import `Bus_Reminder_System.postman_collection.json`
3. Set `baseUrl` variable (default: `http://localhost:8080`)

### Endpoints

- `GET /api/bus-location/health` - Health check
- `POST /api/bus-location/update` - Update bus location

### Example Request

```json
POST http://localhost:8080/api/bus-location/update
Content-Type: application/json

{
    "bus_id": "BUS001",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "timestamp": "2024-01-15T10:30:00Z"
}
```

## Performance Tips

1. **Database:** Use indexes, avoid N+1 queries, batch operations
2. **Kafka:** Tune consumer concurrency, monitor lag
3. **APIs:** Implement retry logic, use connection pooling, cache when appropriate

## Resources

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Kafka Docs](https://spring.io/projects/spring-kafka)
- [Swagger UI](http://localhost:8080/swagger-ui.html) (when running)

