# Overview

This project is a **Spring Boot** microservices demo that simulates a real-world e-commerce platform.
It incorporates **Spring Security** with **OAuth 2.0** via **Keycloak** for authentication and **Spring Cloud Gateway** as the API gateway.
The architecture integrates a modern **observability stack**, including **OpenTelemetry (OTel)**, **Grafana**, **Loki**, **Tempo**, and **Prometheus**.
Deployment is supported through **Docker Compose** for local environments and **Kubernetes** with **Helm** for scalable deployments. 

# Links

📘 API Documentation (Swagger UI) – [http://34.116.220.98:8080/swagger-ui/index.html](http://34.116.220.98:8080/swagger-ui/index.html)

📊 Grafana Dashboard – [http://34.116.204.89:3000/](http://34.116.204.89:3000/)

# Tech Stack

* **Microservices**: User, Product, Cart, Order
* **Service Discovery**: Eureka Server
* **Configuration Management**: Spring Cloud Config Server
* **API Gateway**: Spring Cloud Gateway
* **Authentication**: Spring Security with OAuth 2.0 (Keycloak)
* **Observability**: OpenTelemetry, Grafana, Loki, Tempo, Prometheus
* **Database**: PostgreSQL
* **Deployment**: Docker Compose (local), Kubernetes + Helm (production)
* **Documentation**: Swagger/OpenAPI
