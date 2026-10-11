# ERP System - Complete Microservices Architecture

## System Overview

A production-ready Enterprise Resource Planning (ERP) system built with Spring Boot microservices, Keycloak authentication, PostgreSQL databases, and API Gateway routing.

### Architecture Components

| Component | Port | Role |
|-----------|------|------|
| **Keycloak** | 8080 | OAuth2/JWT authentication & authorization |
| **API Gateway** | 8081 | Request routing, load balancing, security |
| **Eureka Server** | 8761 | Service discovery & registration |
| **Inventory Service** | 8082 | Inventory management & item catalog |
| **Order Service** | 8083 | Order processing & management |
| **PostgreSQL** | 5432+ | Persistent data storage |

## Quick Start

### Development Environment

```bash
# Start all services
docker compose up -d

# Start inventory service
cd inventory-service && docker compose up -d && cd ..

# Start order service
cd order-service && docker compose up -d && cd ..

# Verify services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8761/
```

### Authenticate & Test

```bash
# Get authentication token
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/erp-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=erp-frontend&username=testuser&password=password123&grant_type=password" | jq -r '.access_token')

# Test APIs
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/inventory
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/order
```

## Project Structure

```
.
├── api-gateway/                    # Spring Cloud Gateway
│   ├── src/main/java/.../config/SecurityConfig.java
│   ├── Dockerfile
│   └── pom.xml
├── eureka-server/                  # Service Discovery
│   ├── Dockerfile
│   └── pom.xml
├── inventory-service/              # Microservice
│   ├── src/main/java/.../controller/ItemController.java
│   ├── src/main/resources/application.yml
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── pom.xml
├── order-service/                  # Microservice
│   ├── src/main/resources/application.yml
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml              # Root compose (Keycloak, Eureka)
├── erp-realm.json                  # Keycloak realm configuration
├── .github/workflows/
│   ├── build.yml                   # Build & push to Docker Hub
│   └── test.yml                    # Unit & integration tests
├── DEPLOYMENT.md                   # Deployment guide
├── SECURITY.md                     # Security best practices
├── PRODUCTION_CHECKLIST.md         # Production readiness
├── k8s-prod-deployment.yaml        # Kubernetes manifests
└── .env.example                    # Environment variables template
```

## CI/CD Pipeline

### GitHub Actions Workflows

**Build Pipeline** (`.github/workflows/build.yml`)
- Builds Docker images for all services
- Pushes to Docker Hub (requires secrets)
- Caches layer dependencies for speed
- Triggered on: push to main/develop, path changes

**Test Pipeline** (`.github/workflows/test.yml`)
- Runs Maven builds
- Executes unit & integration tests
- Triggered on: push & pull requests

### Setup GitHub Secrets

```bash
# Add to GitHub repository settings -> Secrets
DOCKER_USERNAME=your_username
DOCKER_PASSWORD=your_access_token
```

### Image Tags

Images follow semantic versioning:
- `yourusername/erp-api-gateway:main-latest`
- `yourusername/erp-api-gateway:v1.2.3`
- `yourusername/erp-api-gateway:main-abc1234` (commit SHA)

## Production Deployment

### Prerequisites
- Docker Compose 2.20+ or Kubernetes 1.24+
- 8GB RAM, 4+ CPU cores
- External reverse proxy (nginx/HAProxy) for TLS
- DNS pointing to your domain

### Option 1: Docker Compose (Single Host)
```bash
cp .env.example .env
# Edit .env with production values
docker compose -f docker-compose.yml up -d
```

See `DEPLOYMENT.md` for detailed instructions.

### Option 2: Kubernetes (Recommended for HA)
```bash
kubectl create ns erp-prod
kubectl apply -f k8s-prod-deployment.yaml
```

Features:
- Horizontal Pod Autoscaler (2-10 replicas)
- Health checks & readiness probes
- Resource limits & requests
- Secrets management
- Rolling updates

## Monitoring & Logging

### Health Endpoints
```bash
# All services expose health checks
GET /actuator/health         # Overall health
GET /actuator/health/ready   # Readiness for traffic
GET /actuator/metrics        # Prometheus metrics
```

### Real-time Container Logs
```bash
docker compose logs -f api-gateway
docker compose logs -f inventory-service
docker compose logs -f keycloak
```

### Kubernetes Logs
```bash
kubectl logs -f deployment/inventory-service -n erp-prod
kubectl logs -f deployment/keycloak -n erp-prod
```

### Metrics Collection (Optional)
```yaml
# Add Prometheus scrape config
- job_name: 'erp-services'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['api-gateway:8081', 'inventory-service:8082', 'order-service:8083']
```

## Security Checklist

- [ ] Rotate Keycloak admin password
- [ ] Update default database passwords
- [ ] Enable TLS/SSL for production domain
- [ ] Configure network policies (Kubernetes)
- [ ] Implement secrets management
- [ ] Enable container image scanning
- [ ] Set resource limits
- [ ] Configure centralized logging
- [ ] Enable audit logging

See `SECURITY.md` for detailed hardening procedures.

## Scaling Strategy

### Horizontal Scaling (Add more instances)
```bash
# Docker Compose
docker compose up -d --scale inventory-service=3

# Kubernetes auto-scaling
kubectl autoscale deployment inventory-service --min=2 --max=10 --cpu-percent=70
```

### Database Scaling
- Read replicas for query scaling
- Connection pooling with PgBouncer
- Database sharding for massive scale

## Backup & Recovery

### Automated Backups
```bash
# Daily backup script
0 2 * * * docker exec keycloak-db pg_dump -U keycloak keycloak > /backups/keycloak_$(date +%Y%m%d).sql
0 3 * * * docker exec postgres-inventory pg_dump -U postgres erp_inventory > /backups/inventory_$(date +%Y%m%d).sql
```

### Restore from Backup
```bash
docker exec -i keycloak-db psql -U keycloak keycloak < keycloak_backup.sql
docker exec -i postgres-inventory psql -U postgres erp_inventory < inventory_backup.sql
```

## Troubleshooting

### Service Won't Start
```bash
docker logs service_name --tail 100
docker compose logs --follow
```

### Network Issues Between Services
```bash
# Test DNS resolution
docker exec api-gateway nslookup inventory-service

# Test connectivity
docker exec api-gateway nc -zv inventory-service 8082
```

### Database Connection Errors
```bash
# Check DB is ready
docker exec postgres-inventory pg_isready -U postgres

# Verify connection string
echo $SPRING_DATASOURCE_URL
```

See `DEPLOYMENT.md` for more troubleshooting steps.

## Next Steps

1. **Configure Docker Hub**
   - Create repository: `erp-api-gateway`, `erp-inventory-service`, etc.
   - Add GitHub secrets for automated pushes

2. **Setup Monitoring**
   - Deploy Prometheus + Grafana
   - Create dashboards for key metrics
   - Setup alert rules

3. **Add More Services**
   - Implement Sales Service (`salesservice/`)
   - Add Payment Service
   - Add Reporting Service

4. **Production Deployment**
   - Deploy to Kubernetes cluster
   - Setup ingress controller (nginx)
   - Configure certificate management (cert-manager)

5. **Performance Tuning**
   - Load testing with k6/JMeter
   - Query optimization
   - Cache strategy (Redis)

6. **Documentation**
   - API documentation (OpenAPI/Swagger)
   - Architecture diagrams (C4 model)
   - Runbooks for operations

## Support & Resources

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Cloud: https://spring.io/projects/spring-cloud
- Keycloak: https://www.keycloak.org/
- Docker: https://docs.docker.com/
- Kubernetes: https://kubernetes.io/docs/
- PostgreSQL: https://www.postgresql.org/docs/

## License

[Your License Here]
