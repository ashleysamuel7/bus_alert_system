FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy Maven build artifact
COPY target/bus-alert-system-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/bus-location/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

