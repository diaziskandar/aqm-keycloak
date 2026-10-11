# ERP System - Deployment Guide

## Prerequisites
- Docker & Docker Compose 2.20+
- 8GB+ RAM, 4+ CPU cores
- Port access: 8080 (Keycloak), 8081 (Gateway), 8761 (Eureka)

## Development Deployment

### 1. Start Services
```bash
# From root directory
docker compose up -d

# Start inventory-service
cd inventory-service && docker compose up -d

# Start order-service
cd order-service && docker compose up -d
```

### 2. Verify Services
```bash
# Check all containers
docker ps -a

# View logs
docker logs keycloak --tail 50
docker logs api-gateway --tail 50
docker logs inventory-service --tail 50
```

### 3. Verify Healthchecks
```bash
# Keycloak
curl http://localhost:8080/health

# API Gateway
curl http://localhost:8081/actuator/health

# Inventory Service
curl http://localhost:8082/actuator/health

# Order Service
curl http://localhost:8083/actuator/health
```

## Production Deployment

### Option 1: Docker Swarm
```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.prod.yml erp

# Check status
docker stack services erp
docker stack ps erp
```

### Option 2: Kubernetes
```bash
# Create namespace
kubectl create ns erp-prod

# Create secrets
kubectl create secret generic db-credentials \
  --from-literal=username=keycloak \
  --from-literal=password=YOUR_SECURE_PASSWORD \
  -n erp-prod

# Deploy services
kubectl apply -f k8s-prod-deployment.yaml

# Check deployment
kubectl get pods -n erp-prod
kubectl logs -f deployment/keycloak -n erp-prod
```

## Scaling

### Docker Compose
```bash
# Scale inventory-service to 3 replicas
docker compose up -d --scale inventory-service=3
```

### Kubernetes
```bash
# Update replicas
kubectl scale deployment inventory-service --replicas=3 -n erp-prod

# Auto-scaling
kubectl autoscale deployment inventory-service \
  --min=2 --max=10 --cpu-percent=70 -n erp-prod
```

## Monitoring

### View Real-time Metrics
```bash
# CPU & Memory usage
docker stats

# Service logs in real-time
docker compose logs -f
```

### Kubernetes Monitoring
```bash
# Pod resources
kubectl top nodes
kubectl top pods -n erp-prod

# Port-forward to Prometheus/Grafana (if deployed)
kubectl port-forward svc/prometheus 9090:9090 -n erp-prod
kubectl port-forward svc/grafana 3000:3000 -n erp-prod
```

## Backup & Recovery

### Database Backup
```bash
# Backup Keycloak DB
docker exec keycloak-db pg_dump -U keycloak keycloak > keycloak_backup.sql

# Backup Inventory DB
docker exec postgres-inventory pg_dump -U postgres erp_inventory > inventory_backup.sql

# Restore
docker exec -i keycloak-db psql -U keycloak keycloak < keycloak_backup.sql
```

## Troubleshooting

### Container Won't Start
```bash
# View logs
docker logs container_name

# Check resource limits
docker stats

# Check network
docker network inspect erp-net
```

### Services Can't Communicate
```bash
# Check DNS resolution
docker exec container_name nslookup service-name

# Check connectivity
docker exec container_name nc -zv service-name port
```

### Database Connection Issues
```bash
# Check DB status
docker exec postgres_container pg_isready -U postgres

# View connection logs
docker logs postgres_container
```

## Cleanup

```bash
# Stop all services
docker compose down

# Remove volumes (WARNING: deletes data)
docker compose down -v

# Remove all ERP images
docker rmi $(docker images | grep erp | awk '{print $3}')
```

## Upgrade Procedure

1. Pull latest images: `docker compose pull`
2. Test in staging environment
3. Blue-green deployment: run new version alongside old
4. Route traffic to new version
5. Monitor for issues
6. Remove old version after verification
