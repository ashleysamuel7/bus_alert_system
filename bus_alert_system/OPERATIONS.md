# Operations Guide

Operational procedures for running and maintaining the Bus Reminder System.

## Service Management

### Start/Stop/Restart

**Docker:**
```bash
docker compose up -d              # Start
docker compose down              # Stop
docker compose restart app        # Restart
```

**Kubernetes:**
```bash
kubectl apply -f k8s/             # Start
kubectl delete deployment bus-reminder-app  # Stop
kubectl rollout restart deployment/bus-reminder-app  # Restart
```

**Systemd:**
```bash
sudo systemctl start bus-reminder
sudo systemctl stop bus-reminder
sudo systemctl restart bus-reminder
```

### Status Check

```bash
curl http://localhost:8080/api/bus-location/health
docker ps | grep bus-reminder
kubectl get pods -l app=bus-reminder
sudo systemctl status bus-reminder
```

## Monitoring

### Logs

**Docker:**
```bash
docker logs -f bus-reminder-app
docker logs --tail 100 bus-reminder-app
```

**Kubernetes:**
```bash
kubectl logs -f deployment/bus-reminder-app
```

**Systemd:**
```bash
sudo journalctl -u bus-reminder -f
```

### Key Log Patterns

**Success:** `Started BusReminderApplication`, `Received bus location event`, `SMS sent`  
**Warnings:** `Twilio not configured`, `Falling back to Haversine`  
**Errors:** `ERROR`, `Exception`, `Connection refused`

### Metrics to Monitor

- Application health, error rate, request throughput
- Kafka consumer lag, message processing rate
- Database connection pool usage, query performance
- External API response times and success rates

## Troubleshooting

### Service Won't Start

**Diagnosis:**
```bash
docker logs bus-reminder-app
docker ps | grep mysql
docker ps | grep kafka
lsof -i :8080
```

**Common Causes:**
- Database/Kafka connection failure
- Port already in use
- Missing environment variables

**Resolution:**
- Verify MySQL and Kafka are running
- Check port conflicts
- Verify all required env vars are set

### No Notifications Sent

**Diagnosis:**
```bash
mysql -u root -p -e "SELECT COUNT(*) FROM bus_passenger WHERE notified = false;"
grep "Calculated ETA" application.log
grep "Twilio" application.log
```

**Common Causes:**
- No passengers in database
- ETA exceeds threshold
- Twilio not configured
- All passengers already notified

**Resolution:**
- Load sample data
- Adjust notification threshold
- Configure Twilio credentials
- Reset `notified` flag for testing

### Kafka Consumer Not Processing

**Diagnosis:**
```bash
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group bus-location-consumer-group \
  --describe
```

**Resolution:**
- Verify consumer group ID
- Create topic if missing
- Check network connectivity
- Restart application

### High Memory Usage

**Resolution:**
- Increase JVM heap: `-Xmx4g -Xms2g`
- Review connection pool settings
- Check for memory leaks
- Scale horizontally

### Database Connection Issues

**Resolution:**
- Increase connection pool size
- Check database server resources
- Review connection timeout settings
- Optimize queries

## Performance Tuning

### Database
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Kafka
```properties
spring.kafka.listener.concurrency=5
spring.kafka.consumer.max-poll-records=500
```

### JVM
```bash
-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

## Backup and Recovery

### Database Backup

```bash
# Full backup
mysqldump -u root -p bus_reminder_db > backup_$(date +%Y%m%d).sql

# Restore
mysql -u root -p bus_reminder_db < backup_20240115.sql
```

### Recovery Procedures

1. **Database Corruption:** Stop app → Restore backup → Verify → Restart
2. **Application Failure:** Check logs → Rollback version → Restart
3. **Data Loss:** Stop immediately → Restore backup → Verify → Resume

## Incident Response

### Severity Levels

- **P1 (Critical):** Service down, data loss, security breach
- **P2 (High):** Partial outage, performance degradation
- **P3 (Medium):** Minor issues, non-critical errors

### Response Process

1. **Identify:** Check health endpoint, review logs, check alerts
2. **Assess:** Determine severity, identify root cause
3. **Contain:** Stop propagation, isolate components
4. **Resolve:** Apply fix, verify resolution, monitor
5. **Post-Incident:** Document, root cause analysis, update procedures

### Common Incidents

**Service Down:**
1. Check health endpoint
2. Review logs
3. Check dependencies
4. Restart if needed

**High Error Rate:**
1. Check error logs
2. Identify pattern
3. Check external API status
4. Apply fix

**Performance Degradation:**
1. Check resource usage
2. Review slow queries
3. Check Kafka consumer lag
4. Scale if needed

## Maintenance

**Weekly:** Review logs, check disk space, verify backups  
**Monthly:** Security updates, dependency updates, performance review  
**Quarterly:** Capacity planning, disaster recovery drill

