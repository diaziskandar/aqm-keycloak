# ERP System - Security Best Practices

## Container Security

### 1. Run as Non-Root User
```dockerfile
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser
```

### 2. Read-Only Root Filesystem
```yaml
services:
  app:
    read_only: true
    volumes:
      - /tmp
      - /var/tmp
```

### 3. Use Minimal Base Images
```dockerfile
FROM eclipse-temurin:21-jdk-alpine  # Multi-stage only
FROM eclipse-temurin:21-jre-alpine  # Production runtime
# Avoid: FROM ubuntu, FROM centos
```

### 4. Security Scanning
```bash
# Scan images for vulnerabilities
docker scan my-image:latest

# Using Trivy
trivy image my-image:latest
```

## Network Security

### 1. Network Policies (Kubernetes)
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: erp-network-policy
spec:
  podSelector:
    matchLabels:
      app: inventory-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: api-gateway
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: keycloak
```

### 2. Service-to-Service mTLS
Configure Spring Cloud Kubernetes for mutual TLS between services.

### 3. Network Segmentation
- Use separate networks for different tiers
- Restrict inter-service communication
- Database only accessible from application tier

## Data Security

### 1. Secrets Management
**DO NOT** hardcode secrets in:
- Environment variables
- Docker images
- Configuration files

**USE INSTEAD:**
- Kubernetes Secrets
- Docker Secrets (Swarm)
- HashiCorp Vault
- AWS Secrets Manager
- Azure Key Vault

Example with Kubernetes:
```bash
kubectl create secret generic db-credentials \
  --from-literal=password=YOUR_SECURE_PASSWORD \
  -n erp-prod
```

### 2. Database Credentials
```yaml
# WRONG - Commits password to repo
POSTGRES_PASSWORD: mypassword

# RIGHT - Reference secret
env:
- name: POSTGRES_PASSWORD
  valueFrom:
    secretKeyRef:
      name: db-credentials
      key: password
```

### 3. Encryption at Rest
- Enable PostgreSQL DIKE extension
- Use encrypted storage volumes
- Backup encryption enabled

### 4. Encryption in Transit
```yaml
# Use TLS for all inter-service communication
ingress:
  tls:
  - hosts:
    - keycloak.yourdomain.com
    secretName: keycloak-tls-cert
```

## Authentication & Authorization

### 1. Keycloak Hardening
```yaml
# Set strong admin credentials
KEYCLOAK_ADMIN: admin
KEYCLOAK_ADMIN_PASSWORD: CHANGE_ME_STRONG_PASSWORD

# Enable HTTPS in production
KC_HOSTNAME_STRICT_HTTPS: true

# Set valid hostname
KC_HOSTNAME: keycloak.yourdomain.com
```

### 2. API Gateway Security
- Enable rate limiting
- Implement OAuth2/JWT validation
- Add request signing
- CORS properly configured

### 3. JWT Token Security
```java
// Spring Security - validate JWT issuer
.issuer("https://keycloak.yourdomain.com/realms/erp-realm")

// Set token expiration
// Keep it short (5-15 minutes) - use refresh tokens
```

## API Security

### 1. API Rate Limiting
```yaml
# Spring Cloud Gateway rate limiter
spring:
  cloud:
    gateway:
      routes:
        - id: inventory-service
          uri: lb://inventory-service
          predicates:
            - Path=/api/inventory/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
```

### 2. Input Validation
- Validate all input at gateway level
- Sanitize requests
- Set size limits for uploads

### 3. API Versioning
```
/api/v1/inventory    # Stable version
/api/v2-beta/order   # Beta features
```

## Logging & Monitoring

### 1. Audit Logging
- Log all authentication attempts
- Log all API calls (method, endpoint, user, timestamp)
- Log all database modifications

### 2. Centralized Logging
```yaml
# Use ELK stack or Loki
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
    labels: "service=erp"
```

### 3. Alert Rules
- Failed login attempts (> 5 in 10 minutes)
- Database connection errors
- Service restart loops
- High CPU/Memory usage (> 90%)
- Unusual API traffic patterns

## Compliance

### 1. Data Privacy
- GDPR: Personal data handling procedures
- Right to deletion: Implement data purge
- Data export: API to export user data

### 2. Audit Trail
- Immutable logs of all changes
- Retain logs for 90+ days
- Regular log reviews

### 3. Regular Security Assessments
- Quarterly vulnerability scans
- Annual penetration testing
- Code security reviews

## Incident Response

### 1. Incident Procedures
1. Detect & Alert
2. Isolate affected services
3. Preserve logs & evidence
4. Notify stakeholders
5. Remediate
6. Post-incident review

### 2. Emergency Contacts
- Security team lead: security@company.com
- Infrastructure team: ops@company.com
- Incident escalation procedure

### 3. Disaster Recovery
- Backup database daily
- Test restore procedures monthly
- Keep 30+ days backup history
