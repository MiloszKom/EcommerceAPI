# Overview

This project is a **Spring Boot** microservices demo that simulates a real-world e-commerce platform.
It incorporates **Spring Security** with **OAuth 2.0** via **Keycloak** for authentication and **Spring Cloud Gateway** as the API gateway.
The architecture integrates a modern **observability stack**, including **OpenTelemetry (OTel)**, **Grafana**, **Loki**, **Tempo**, and **Prometheus**.
Deployment is supported through **Docker Compose** for local environments and **Kubernetes** with **Helm** for scalable deployments. 

> **Note**: The public endpoints are currently offline.  
> The GKE cluster was running for nearly a month and my free Google Cloud credits are almost depleted. Since hosting a Kubernetes cluster is costly, it had to be temporarily shut down.

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

# Screenshots

1. All Services Deployed on Google Cloud
<img width="1120" height="828" alt="Image" src="https://github.com/user-attachments/assets/30bfecd8-88d0-478e-9d3a-f5aef81c5dcb" />
<br><br>
2. Kubernetes Dashboard (Local Deployment)
<br><br>
<img width="1898" height="906" alt="Image" src="https://github.com/user-attachments/assets/079d4702-012b-4800-9b68-65231def82ef" />
<br><br>
3. Swagger API Documentation
<br><br>
<img width="1645" height="905" alt="Image" src="https://github.com/user-attachments/assets/468503a0-0cff-4700-b163-a276ba5fc31e" />
