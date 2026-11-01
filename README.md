# Aurora Commerce | Dağıtık E-Ticaret Mikroservis Platformu

[![CI](https://github.com/ensar-sencan/aurora-commerce/actions/workflows/ci.yml/badge.svg)](https://github.com/ensar-sencan/aurora-commerce/actions/workflows/ci.yml)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%20%7C%204.1-brightgreen.svg?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red.svg?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)
![Swagger](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D.svg?logo=swagger)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange)

> **Aurora Commerce**, yüksek trafikli e-ticaret sistemlerindeki veri tutarsızlığı (data inconsistency) ve mükerrer sipariş (double-spending) problemlerini çözmek amacıyla **Spring Boot 3** kullanılarak geliştirilmiş, üretime hazır (production-grade) bir mikroservis projesidir.

---

## Sistem Mimarisi (Architecture)

Proje, monolitik yapılardaki darboğazları aşmak için **Schema-per-service** izolasyon prensibiyle tasarlanmıştır. Her servis kendi veritabanı şemasından sorumludur ve çapraz sorgulara (cross-query) izin verilmez. Servisler arası iletişim **FeignClient** üzerinden sağlanmaktadır.

```mermaid
graph TD
    Client([İstemci / Postman])

    subgraph Microservices Layer
        Auth[🔐 Auth Service :8081]
        Product[📦 Product Service :8082]
        Order[🛒 Order Service :8083]
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

    Order -->|Cart, TTL 24h| Redis
    Order <-->|5. Feign: Deduct / Restore Stock| Product
```

---

## Çözülen Temel Mühendislik Problemleri

### 1. Dağıtık Sistemlerde Veri Tutarlılığı (Saga Pattern)

Servisler arası sınırda klasik veritabanı transaction'ları (`@Transactional`) çalışmadığı için **Compensating Action (Telafi İşlemi)** tabanlı Saga deseni uygulanmıştır.

- **Akış:** Sipariş oluşturulmadan önce `product-service` üzerinden stok düşülür.
- **Telafi:** Siparişin veritabanına yazılması aşamasında bir hata oluşursa (örn. ağ kesintisi, idempotency çakışması), `product-service`'e telafi isteği (`/internal/stock/restore`) atılarak stoklar anında geri yüklenir. Yarım kalan (orphan) sipariş durumu yapısal olarak imkansızlaştırılmıştır.

### 2. ACID Uyumlu Stok Yönetimi (Oversell Protection)

Stok düşüm işlemlerinde JPA'nın zafiyet yaratabilen "Read-Modify-Write" anti-pattern'i yerine doğrudan veritabanı kilitlemesi (database-level lock) kullanılmıştır. Negatif stoğu engellemek ve fiyatı aynı işlemde güvenle yakalamak için atomik SQL yazılmıştır:

```sql
UPDATE product.products
SET stock = stock - :qty
WHERE id = :id AND stock >= :qty
RETURNING unit_price;
```

Etkilenen satır sayısı `0` dönerse istek anında `OutOfStockException` ile durdurulur; hiçbir yazma işlemi gerçekleşmeden `409` yanıtı üretilir.

### 3. Çift Tıklama Koruması (Idempotency)

Kullanıcının ödeme tuşuna art arda basması veya ağdaki retry mekanizmaları sebebiyle mükerrer sipariş oluşmasını engellemek için iki katmanlı koruma uygulanmıştır:

- **Birinci katman (uygulama):** Checkout, iş yapmadan önce `Idempotency-Key` ile mevcut siparişi arar. Bulursa ve sahibi aynı müşteriyse **ilk siparişi aynen** döner (stok, sepet ve cache'e dokunulmaz). Anahtar başka bir müşteriye aitse `409 Conflict` döner, böylece bir müşteri başkasının siparişini göremez.
- **Son savunma hattı (veritabanı):** `orders` tablosundaki `idempotency_key` alanında **unique index** vardır. Aynı anda gelen iki istek ilk kontrolü birlikte geçerse `saveAndFlush()` bu index'e çarpar ve hata saga telafi akışını (stok iade) tetikler.

### 4. Servisler Arası Güvenlik (Internal Token)

`product-service` üzerindeki `/internal/stock/deduct` ve `/internal/stock/restore` uç noktaları yalnızca `order-service`'in çağırması için tasarlanmış iç uç noktalardır. `order-service` her çağrıda `X-Internal-Token` header'ını gönderir.

> **Bilinen eksik:** `product-service` tarafında bu header'ı doğrulayan bir filtre henüz yoktur, yani bu uç noktalar şu an teknik olarak korumasızdır. Doğrulama filtresi yol haritasındadır (secret'ın ortam değişkeninden okunması ile birlikte).

---

## Teknoloji Yığını (Tech Stack)

- **Backend:** Java 17, Spring Boot 3.3 (auth, product) / 4.1 (order), Spring Data JPA, Spring Security (auth, order), OpenFeign
- **CI:** GitHub Actions (her push'ta üç servis için matrix build)
- **Veritabanı & Önbellek:** PostgreSQL 16, Redis 7 (Cache-aside: `@Cacheable` / `@CacheEvict` ile ürün kataloğu önbellekleme, sepet için `@RedisHash` + TTL)
- **Veritabanı Migrasyonu:** Liquibase (Changeset bazlı sürüm yönetimi + servis başlangıcında otomatik seed data)
- **Kimlik Doğrulama:** JWT (JSON Web Token - HS256) tabanlı Stateless Auth
- **Nesne Dönüşümü:** MapStruct (DTO ↔ Entity mapping, product-service)
- **API Dokümantasyonu:** springdoc-openapi (Swagger UI)
- **Gözlemlenebilirlik:** Spring Boot Actuator (health check, product-service)
- **Altyapı:** Docker (multi-stage build) & Docker Compose

---

## Kurulum ve Çalıştırma (Getting Started)

Altyapı (PostgreSQL ve Redis) Docker Compose ile ayağa kalkar, servisler yerelde Maven wrapper ile çalıştırılır. Üç servisin de Dockerfile'ı hazırdır, hepsinin tek komutla Compose'a alınması yol haritasındadır.

### Ön Koşullar

- Docker & Docker Compose
- Java JDK 17+ (Maven kurmak gerekmez, her serviste `mvnw` vardır)

### 1. Altyapıyı Başlatın (DB & Redis)

Öncelikle veritabanı şemalarının ve Redis'in ayağa kalkması gerekmektedir. Ana dizinde terminali açın:

```bash
docker compose up -d
```

*(Bu işlem, PostgreSQL içinde `auth`, `product` ve `orders` şemalarını izole bir şekilde otomatik oluşturur ve Redis'i başlatır. PostgreSQL makinenizde **5433** portundan, Redis **6379**'dan erişilir.)*

### 2. Mikroservisleri Ayağa Kaldırın

Her servis ayrı bir Maven projesidir. Üç ayrı terminalde sırasıyla başlatın:

```bash
cd auth-service && ./mvnw spring-boot:run
```

```bash
cd product-service && ./mvnw spring-boot:run
```

```bash
cd order-service && ./mvnw spring-boot:run
```

*(Uygulama ayağa kalkarken Liquibase, her servisin kendi `db.changelog-master.yaml` dosyası üzerinden tabloları ve varsayılan test verilerini otomatik olarak veritabanına işleyecektir.)*

### 3. API Dokümantasyonuna Erişim (Swagger UI)

Her servis ayağa kalktıktan sonra, o servisin interaktif API dokümantasyonuna tarayıcı üzerinden erişilebilir:

- Auth Service: `http://localhost:8081/swagger-ui.html`
- Product Service: `http://localhost:8082/swagger-ui.html`
- Order Service: `http://localhost:8083/swagger-ui.html`

### 4. Servisleri Docker İmajı Olarak Çalıştırma

Her mikroservis, iki aşamalı (multi-stage) bir Dockerfile içerir: ilk aşamada `maven:3.9-eclipse-temurin-17` imajı ile proje derlenir, ikinci aşamada yalnızca üretilen `.jar` dosyası hafif bir `eclipse-temurin:17-jre-alpine` çalışma zamanına kopyalanır. Bu sayede nihai imaj boyutu küçük tutulur.

```bash
docker build -t aurora/auth-service ./auth-service
docker build -t aurora/product-service ./product-service
docker build -t aurora/order-service ./order-service
```

---

## API Akışı ve Test Senaryoları (Postman)

Bu projeyi test etmek için sisteme Postman koleksiyonu entegre edilebilir. Temel akış şu şekildedir:

1. **Kayıt Ol / Giriş Yap:** `POST /auth/register` ve `POST /auth/login` (Dönen JWT token'ı Header'a ekleyin).
2. **Kataloğu İncele:** `GET /products` (Public erişim, cache-aside mekanizması ile çalışır).
3. **Sepete Ürün Ekle:** `POST http://localhost:8083/api/v1/cart/items` (Redis TTL ile 24 saat saklanır).
4. **Checkout (Sipariş Onayı):** `POST http://localhost:8083/api/v1/orders/checkout` (Zorunlu `Idempotency-Key` başlığı ile).
5. **Sipariş Geçmişi:** `GET http://localhost:8083/api/v1/orders`.

---

## 👨‍💻 Geliştirici Notu

Bu proje, staj bitirme projesi kapsamında; modern mikroservis mimarilerinde karşılaşılan zorlukları (servisler arası iletişim, güvenli yetkilendirme, veri bütünlüğü) anlamak ve "Best Practice" standartlarına uygun profesyonel çözümler üretmek amacıyla geliştirilmiştir.

İncelediğiniz için teşekkürler!
