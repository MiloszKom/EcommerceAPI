# Overview

This project is a **Spring Boot** microservices demo that simulates a real-world e-commerce platform.
It incorporates **Spring Security** with **OAuth 2.0** via **Keycloak** for authentication and **Spring Cloud Gateway** as the API gateway.
The architecture integrates a modern **observability stack**, including **OpenTelemetry (OTel)**, **Grafana**, **Loki**, **Tempo**, and **Prometheus**.
Deployment is supported through **Docker Compose** for local environments and **Kubernetes** with **Helm** for scalable deployments. 

> **Deployment Status**: Currently in active development.  

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
