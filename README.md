# TicketWizard

**TicketWizard** is a professional-grade backend ticket broker platform (a Ticketmaster-lite) designed to handle the complexities of high-concurrency ticket reservations and flash sales. Built as a modular monolith, this project goes beyond standard CRUD operations to tackle real-world engineering challenges such as preventing overselling, implementing rate limiting, asynchronous message processing, and comprehensive observability.

## 🚀 Key Features & Engineering Challenges

### 1. Flash Sale & Overselling Prevention
The core challenge of this platform is surviving "flash-sale" traffic—thousands of concurrent users competing for limited seats without double-booking.
- **Atomic Operations**: Employs DB-level guarantees and locking strategies to ensure the inventory invariant (`available + held + sold = total`) is strictly maintained under heavy concurrent load.
- **Reservation Holds**: Implements a short-lived hold system (e.g., 5-minute reservations) backed by Redis to lock seats during the checkout flow prior to payment confirmation.

### 2. Modular Monolith Architecture
Microservices solve organizational scalability problems, but for a single team/project, they introduce an unnecessary distributed systems tax. TicketWizard uses a **Modular Monolith** architecture:
- **Async Communication**: Leverages Spring Modulith and RabbitMQ to decouple domains (e.g., Checkout, Payments, Notifications). Synchronous calls are reserved for the critical request path, while side-effects run asynchronously.
- **Future-Proof**: Module boundaries are explicitly defined, allowing any domain to be seamlessly extracted into a standalone microservice if needed.

### 3. Traffic Control & Queueing
- **Rate Limiting**: Protects high-contention endpoints (like checkout) from bot traffic and refresh-spam using Redis-backed rate limiting algorithms (e.g., Token Bucket / Sliding Window).
- **Virtual Waiting Room**: Controls the admission rate during a flash sale, queueing users and processing them in manageable batches to protect the backend database from being overwhelmed.

### 4. Advanced Entity Modeling & Validation
- **Strict Validations**: Uses a Chain of Responsibility pattern for input validation and database-level constraints (like partial unique indexes) to prevent check-then-act race conditions.
- **Extensible Schema**: Designed with tiered ticketing (General, VIP) mapped cleanly to the Java domain layer, with strict schema evolution managed via Flyway.

### 5. Observability & Load Testing
Built not just to work, but to be proven under stress.
- **Metrics & Tracing**: Integrates Prometheus and Grafana for real-time dashboards tracking request latency (p50/p95/p99), error rates, and queue depth.
- **Load Benchmarks**: Uses tools like k6/Gatling with Testcontainers to simulate flash-sale traffic and compare the system's performance before and after mitigation strategies.

## 🛠 Tech Stack

- **Framework**: Java 21, Spring Boot, Spring Modulith
- **Database**: PostgreSQL (Migrations managed by Flyway)
- **Caching & Rate Limiting**: Redis
- **Message Broker**: RabbitMQ
- **Observability**: Prometheus, Grafana
- **Testing**: JUnit, Testcontainers, k6 / Gatling

## 🗄️ Database Design Highlights

- **Inventory Tracking**: Instead of locking individual seats in a complex map, we track inventory via `TicketType` counters (`available`, `held`, `sold`). This translates the flash sale into a direct concurrent decrement challenge, allowing us to focus on atomic counter updates.
- **Eliminating Race Conditions**: We ensure a user can only have one active `HELD` reservation for an event by relying on a partial unique index (`WHERE status = 'HELD'`). This relies on PostgreSQL's atomic constraints rather than vulnerable application-level checks.

## 🏃‍♂️ Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21+

### Running the Application
1. **Infrastructure**: Start the database, Redis, and RabbitMQ via Docker Compose:
   ```bash
   docker compose up -d
   ```
2. **Environment Configuration**: Create a `.env` file based on `.env.example` in the root directory.
3. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

*(For detailed architectural decisions and the entity model, check the `guide docs/` folder).*
