#  Aurora Commerce | Microservices E-Commerce Pipeline

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen.svg?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red.svg?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange)

> **Aurora Commerce**, yüksek trafikli e-ticaret sistemlerindeki veri tutarsızlığı (data inconsistency) ve mükerrer sipariş (double-spending) problemlerini çözmek amacıyla **Spring Boot 3** kullanılarak geliştirilmiş, üretime hazır (production-grade) bir mikroservis projesidir.

---

##  Mimari Tasarım (Architecture)

Proje, monolitik yapılardaki darboğazları aşmak için **Schema-per-service** izolasyon prensibiyle tasarlanmıştır. Her servis kendi veritabanı şemasından sorumludur ve çapraz sorgulara (cross-query) izin verilmez. Servisler arası iletişim **FeignClient** üzerinden sağlanmaktadır.

```mermaid
graph TD
    Client([ İstemci / Postman])
    
    subgraph Microservices Layer
        Auth[ Auth Service :8081]
        Product[ Product Service :8082]
        Order[ Order Service :8083]
    end

    subgraph Data Layer
        DB_Auth[(PostgreSQL: auth)]
        DB_Prod[(PostgreSQL: product)]
        DB_Ord[(PostgreSQL: orders)]
        Redis[(Redis 7)]
    end

    Client -->|1. Register/Login| Auth
    Client -->|2. Get Catalog| Product
    Client -->|3. Add to Cart| Order
    Client -->|4. Checkout| Order

    Auth -->|Validates/Writes| DB_Auth
    Product -->|Reads/Writes| DB_Prod
    Order -->|Reads/Writes| DB_Ord
    
    Order -->|Cart & Idempotency| Redis
    Order <-->|5. Feign: Deduct / Restore Stock| Product
