# Deployment Artifacts — CI/CD & Kubernetes Manifests

## Overview

This document provides the complete, developer-ready deployment artifacts for the Enterprise Insurance Platform. It includes CI/CD pipeline configuration, Kubernetes manifests, Dockerfiles, and environment-specific configurations.

---

## 1. Dockerfiles

### 1.1 Base Service Dockerfile

```dockerfile
# Dockerfile — Standard service image
FROM eclipse-temurin:21-jre-alpine AS builder

# Non-root user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR
COPY target/*.jar app.jar

# Extract layers for optimized Docker caching
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy extracted layers
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+UseZGC", \
  "-XX:ZUncommitDelay=300", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILE}", \
  "-jar", "/app/app.jar"]
```

### 1.2 Multi-Stage Build with Maven

```dockerfile
# Dockerfile.build — Full CI build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# Security scan
RUN mvn org.owasp:dependency-check-maven:check -B

# Test stage
FROM build AS test
RUN mvn test -B

# Final image
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build --chown=appuser:appgroup /build/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75", \
  "-Dspring.profiles.active=${SPRING_PROFILE}", \
  "-jar", "/app/app.jar"]
```

---

## 2. Kubernetes Manifests

### 2.1 Namespace Configuration

```yaml
# k8s/namespaces.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: insurance-app
---
apiVersion: v1
kind: Namespace
metadata:
  name: insurance-data
---
apiVersion: v1
kind: Namespace
metadata:
  name: insurance-obs
---
apiVersion: v1
kind: Namespace
metadata:
  name: insurance-ingress
---
apiVersion: v1
kind: Namespace
metadata:
  name: insurance-identity
```

### 2.2 Service Account

```yaml
# k8s/service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: policy-service-sa
  namespace: insurance-app
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: policy-service-role
  namespace: insurance-app
rules:
  - apiGroups: [""]
    resources: ["configmaps", "secrets"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: policy-service-rolebinding
  namespace: insurance-app
subjects:
  - kind: ServiceAccount
    name: policy-service-sa
roleRef:
  kind: Role
  name: policy-service-role
  apiGroup: rbac.authorization.k8s.io
```

### 2.3 ConfigMap

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: policy-service-config
  namespace: insurance-app
data:
  SPRING_PROFILE: "dev"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres-service.insurance-data:5432/insurance"
  SPRING_DATASOURCE_HOST: "postgres-service.insurance-data"
  SPRING_DATASOURCE_PORT: "5432"
  SPRING_DATASOURCE_DATABASE: "insurance"
  SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka-service.insurance-data:9092"
  APP_KEYCLOAK_ISSUER_URI: "https://auth.insurance.sa/realms/insurance-platform"
  APP_YAKEEN_BASE_URL: "https://sandbox.yakeen.sa/api/v1"
  APP_NAJM_WSDL_URL: "https://sandbox.najm.sa/najmws/AccidentHistoryService?wsdl"
  APP_PAYMENT_GATEWAY_URL: "https://sandbox.mada-gateway.sa/api/v1"
  APP_SADAD_SFTP_HOST: "sadad-sftp.sama.sa"
  APP_SADAD_SFTP_PORT: "22"
  APP_SADAD_BILLER_ID: "INS_001"
  LOGGING_LEVEL_COM_INSURANCE: "DEBUG"
  MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: "health,metrics,prometheus"
```

### 2.4 Secret Template

```yaml
# k8s/secret-template.yaml
# NOTE: Actual secrets are injected via External Secrets Operator from Vault
# This file documents the required secret keys only
apiVersion: v1
kind: Secret
metadata:
  name: policy-service-secrets
  namespace: insurance-app
type: Opaque
stringData:
  SPRING_DATASOURCE_PASSWORD: "<from-vault>"
  APP_YAKEEN_API_KEY: "<from-vault>"
  APP_YAKEEN_CLIENT_CERT: "<from-vault>"
  APP_NAJM_USERNAME: "<from-vault>"
  APP_NAJM_PASSWORD: "<from-vault>"
  APP_PAYMENT_GATEWAY_API_KEY: "<from-vault>"
  APP_PAYMENT_GATEWAY_SECRET: "<from-vault>"
  APP_JWT_SECRET: "<from-vault>"
  APP_SADAD_SFTP_USERNAME: "<from-vault>"
  APP_SADAD_SFTP_PASSWORD: "<from-vault>"
  APP_SADAD_CERTIFICATE: "<from-vault>"
```

### 2.5 Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: policy-service
  namespace: insurance-app
  labels:
    app: policy-service
    version: "1.0.0"
    managed-by: "argocd"
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
  selector:
    matchLabels:
      app: policy-service
  template:
    metadata:
      labels:
        app: policy-service
        version: "1.0.0"
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: policy-service-sa
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
        seccompProfile:
          type: RuntimeDefault
      containers:
        - name: policy-service
          image: ghcr.io/offer-core/policy-service:${VERSION}
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              protocol: TCP
            - containerPort: 8443
              protocol: TCP
          envFrom:
            - configMapRef:
                name: policy-service-config
            - secretRef:
                name: policy-service-secrets
          env:
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: POD_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
            - name: NODE_IP
              valueFrom:
                fieldRef:
                  fieldPath: status.hostIP
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
            timeoutSeconds: 3
            failureThreshold: 3
          startupProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
            failureThreshold: 30
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          volumeMounts:
            - name: tmp
              mountPath: /tmp
          lifecycle:
            preStop:
              exec:
                command: ["sh", "-c", "sleep 10"]
      volumes:
        - name: tmp
          emptyDir: {}
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values:
                        - policy-service
                topologyKey: kubernetes.io/hostname
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: ScheduleAnyway
          labelSelector:
            matchLabels:
              app: policy-service
```

### 2.6 Service

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: policy-service
  namespace: insurance-app
  labels:
    app: policy-service
spec:
  type: ClusterIP
  ports:
    - name: http
      port: 8080
      targetPort: 8080
      protocol: TCP
    - name: management
      port: 8443
      targetPort: 8443
      protocol: TCP
  selector:
    app: policy-service
```

### 2.7 Horizontal Pod Autoscaler

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: policy-service-hpa
  namespace: insurance-app
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: policy-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 50
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
        - type: Percent
          value: 100
          periodSeconds: 60
        - type: Pods
          value: 4
          periodSeconds: 60
      selectPolicy: Max
```

### 2.8 PodDisruptionBudget

```yaml
# k8s/pdb.yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: policy-service-pdb
  namespace: insurance-app
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: policy-service
```

### 2.9 NetworkPolicy

```yaml
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: policy-service-network-policy
  namespace: insurance-app
spec:
  podSelector:
    matchLabels:
      app: policy-service
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: insurance-ingress
        - podSelector:
            matchLabels:
              app: api-gateway
      ports:
        - protocol: TCP
          port: 8080
  egress:
    - to:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: insurance-data
      ports:
        - protocol: TCP
          port: 5432
        - protocol: TCP
          port: 9092
    - to:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: insurance-identity
      ports:
        - protocol: TCP
          port: 8080
```

### 2.10 Ingress

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: insurance-ingress
  namespace: insurance-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/enable-cors: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "https://app.insurance.sa"
    nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, PUT, PATCH, DELETE, OPTIONS"
    nginx.ingress.kubernetes.io/cors-allow-headers: "Authorization, Content-Type, X-Tenant-ID, X-Correlation-ID"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
    - hosts:
        - api.insurance.sa
      secretName: insurance-tls
  rules:
    - host: api.insurance.sa
      http:
        paths:
          - path: /api/v1/policies
            pathType: Prefix
            backend:
              service:
                name: policy-service
                port:
                  number: 8080
          - path: /api/v1/claims
            pathType: Prefix
            backend:
              service:
                name: claims-service
                port:
                  number: 8080
          - path: /api/v1/billing
            pathType: Prefix
            backend:
              service:
                name: billing-service
                port:
                  number: 8080
          - path: /api/v1/auth
            pathType: Prefix
            backend:
              service:
                name: auth-service
                port:
                  number: 8080
          - path: /api/v1/metadata
            pathType: Prefix
            backend:
              service:
                name: metadata-service
                port:
                  number: 8080
          - path: /actuator
            pathType: Prefix
            backend:
              service:
                name: policy-service
                port:
                  number: 8080
```

---

## 3. CI/CD Pipeline (GitHub Actions)

### 3.1 Build and Deploy Workflow

```yaml
# .github/workflows/deploy.yml
name: Build, Test, and Deploy

on:
  push:
    branches: [develop, main]
  pull_request:
    branches: [develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Run tests and quality gate
        run: |
          mvn verify -B
          mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}

      - name: OWASP Dependency Check
        run: mvn org.owasp:dependency-check-maven:check -B

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

  docker-build-and-scan:
    needs: build-and-test
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,format=short
            type=ref,event=branch
            type=ref,event=pr
            type=semver,pattern={{version}}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ steps.meta.outputs.version }}
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'

      - name: Upload Trivy results
        if: always()
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: 'trivy-results.sarif'

  deploy-dev:
    needs: docker-build-and-scan
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up kubectl
        uses: azure/setup-kubectl@v4

      - name: Configure Kubeconfig
        run: |
          mkdir -p $HOME/.kube
          echo "${{ secrets.KUBE_CONFIG_DEV }}" | base64 -d > $HOME/.kube/config

      - name: Deploy to Dev
        run: |
          kubectl set image deployment/policy-service \
            -n insurance-app \
            policy-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

      - name: Verify deployment
        run: |
          kubectl rollout status deployment/policy-service -n insurance-app --timeout=5m

  deploy-staging:
    needs: deploy-dev
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment:
      name: staging
      url: https://staging.insurance.sa
    steps:
      - uses: actions/checkout@v4

      - name: Configure Kubeconfig
        run: |
          mkdir -p $HOME/.kube
          echo "${{ secrets.KUBE_CONFIG_STAGING }}" | base64 -d > $HOME/.kube/config

      - name: Deploy to Staging
        run: |
          kubectl set image deployment/policy-service \
            -n insurance-app \
            policy-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

      - name: Run smoke tests
        run: |
          ./scripts/smoke-test.sh https://staging.insurance.sa

  deploy-production:
    needs: deploy-staging
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment:
      name: production
      url: https://api.insurance.sa
    steps:
      - uses: actions/checkout@v4

      - name: Configure Kubeconfig
        run: |
          mkdir -p $HOME/.kube
          echo "${{ secrets.KUBE_CONFIG_PROD }}" | base64 -d > $HOME/.kube/config

      - name: Deploy to Production
        run: |
          kubectl set image deployment/policy-service \
            -n insurance-app \
            policy-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}

      - name: Verify production deployment
        run: |
          kubectl rollout status deployment/policy-service -n insurance-app --timeout=10m

      - name: Run post-deployment tests
        run: |
          ./scripts/smoke-test.sh https://api.insurance.sa
```

### 3.2 Helm Chart Structure

```
helm/
  policy-service/
    Chart.yaml
    values.yaml
    values-dev.yaml
    values-staging.yaml
    values-prod.yaml
    templates/
      _helpers.tpl
      deployment.yaml
      service.yaml
      hpa.yaml
      configmap.yaml
      secret.yaml
      pdb.yaml
      serviceaccount.yaml
      ingress.yaml
      networkpolicy.yaml
```

### 3.3 Helm Values Example

```yaml
# helm/policy-service/values.yaml
replicaCount: 2

image:
  repository: ghcr.io/offer-core/policy-service
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080
  managementPort: 8443

resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

probes:
  readiness:
    initialDelaySeconds: 20
    periodSeconds: 10
  liveness:
    initialDelaySeconds: 30
    periodSeconds: 15
  startup:
    initialDelaySeconds: 10
    periodSeconds: 5
    failureThreshold: 30

config:
  SPRING_PROFILE: "dev"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres-service:5432/insurance"
  SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
  APP_KEYCLOAK_ISSUER_URI: "https://auth.insurance.sa/realms/insurance-platform"

secrets:
  SPRING_DATASOURCE_PASSWORD: ""
  APP_YAKEEN_API_KEY: ""
  APP_NAJM_USERNAME: ""
  APP_NAJM_PASSWORD: ""

ingress:
  enabled: true
  host: api.insurance.sa
  tls:
    enabled: true
    secretName: insurance-tls

networkPolicy:
  enabled: true

podDisruptionBudget:
  enabled: true
  minAvailable: 1
```

---

## 4. Environment-Specific Configuration

### 4.1 Development

```yaml
# helm/policy-service/values-dev.yaml
replicaCount: 1

resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"

autoscaling:
  enabled: false

config:
  SPRING_PROFILE: "dev"
  LOGGING_LEVEL_COM_INSURANCE: "DEBUG"

ingress:
  enabled: false
```

### 4.2 Staging

```yaml
# helm/policy-service/values-staging.yaml
replicaCount: 2

resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 5

config:
  SPRING_PROFILE: "staging"
  LOGGING_LEVEL_COM_INSURANCE: "INFO"

ingress:
  enabled: true
  host: staging.insurance.sa
```

### 4.3 Production

```yaml
# helm/policy-service/values-prod.yaml
replicaCount: 3

resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "2000m"

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

config:
  SPRING_PROFILE: "prod"
  LOGGING_LEVEL_COM_INSURANCE: "WARN"

ingress:
  enabled: true
  host: api.insurance.sa

podDisruptionBudget:
  enabled: true
  minAvailable: 2
```

---

## 5. PostgreSQL StatefulSet

```yaml
# k8s/postgres-statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: insurance-data
spec:
  serviceName: postgres
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: "insurance"
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: postgres-secrets
                  key: POSTGRES_USER
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secrets
                  key: POSTGRES_PASSWORD
            - name: PGDATA
              value: /var/lib/postgresql/data/pgdata
          volumeMounts:
            - name: postgres-data
              mountPath: /var/lib/postgresql/data
          resources:
            requests:
              memory: "2Gi"
              cpu: "500m"
            limits:
              memory: "4Gi"
              cpu: "2000m"
          livenessProbe:
            exec:
              command: ["pg_isready", "-U", "insurance"]
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command: ["pg_isready", "-U", "insurance"]
            initialDelaySeconds: 5
            periodSeconds: 5
  volumeClaimTemplates:
    - metadata:
        name: postgres-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 100Gi
        storageClassName: premium-ssd
```

---

## 6. Rollback Procedure

### 6.1 Rollback Commands

```bash
# Rollback to previous version
kubectl rollout undo deployment/policy-service -n insurance-app

# Rollback to specific revision
kubectl rollout undo deployment/policy-service -n insurance-app --to-revision=3

# Check rollout history
kubectl rollout history deployment/policy-service -n insurance-app

# Check rollout status
kubectl rollout status deployment/policy-service -n insurance-app

# Helm rollback
helm rollback policy-service -n insurance-app 1
```

### 6.2 Rollback Checklist

- [ ] Identify the issue (monitoring alerts, user reports)
- [ ] Assess impact (partial vs. full outage)
- [ ] Decide: rollback vs. forward fix
- [ ] Execute rollback command
- [ ] Verify service health (readiness/liveness probes)
- [ ] Verify data integrity (no partial writes)
- [ ] Notify stakeholders
- [ ] Create incident ticket
- [ ] Post-mortem analysis

---

## Document Maintenance

| Aspect | Detail |
|--------|--------|
| Last Updated | 2026-07-06 |
| Owner | DevOps / Platform Team |
| Review Cycle | Monthly, or when infrastructure changes |
| Related Documents | [Deployment Architecture](deployment-architecture.md), [Operations and Observability](operations-observability.md) |
