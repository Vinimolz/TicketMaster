# Event Ticket Broker — Requirements Roadmap

> Purpose of this doc: a living reference for the project. Each module below is intentionally summarized — the goal is to capture *what it does* and *why it's hard*, not to design it in full. Each module is meant to be picked up and deep-dived in its own conversation later.

---

## 1. Project Overview

**What it is:** A ticket broker platform (think Ticketmaster-lite) where users browse events, reserve/buy seats, and the system has to survive **flash-sale traffic** — thousands of concurrent users competing for a limited number of seats at a known start time.

**Why this project:** It combines classic e-commerce backend complexity (accounts, catalog, orders, payments) with a concurrency/scale problem that has a clear, demonstrable "before vs after" story — which CRUD apps don't naturally have.

**Primary hard problem (the centerpiece):** Preventing overselling when many concurrent requests compete for limited inventory, under deliberately simulated high load.

**Architecture decision:** Modular monolith, single deployable, with **async communication between internal modules** for side-effect flows (notifications, queue admission, analytics) rather than synchronous calls. Module boundaries are designed so any module *could* be extracted into its own service later — but we are not doing that now, deliberately, since there's no team/scaling reason to pay the distributed-systems tax yet.

**Why a monolith, stated plainly (for interviews):** Microservices solve organizational problems (independent teams/deploys/scaling) this project doesn't have. The valuable part to demonstrate is the *async, event-driven decoupling pattern* itself — not the operational overhead of running it across networked services.

**Candidate stack:** Spring Boot, Spring Modulith (in-process module boundaries + async events), RabbitMQ (for flows where a real broker adds value), PostgreSQL, Redis (rate limiting / seat holds), Testcontainers + a load-testing tool (k6 or Gatling) for benchmarking, Prometheus + Grafana for observability.

---

## 2. Module Breakdown

Each module below has: **purpose**, **main problems/challenges**, **sync vs async role**, and **status**.

### 2.1 Catalog & Inventory (Events, TicketTypes, Tickets)
- **Purpose:** Manage events, ticket tiers/pricing, and availability.
- **Scope decision (locked):** General admission only — no seat maps or assigned seating in v1. The contested resource is the `TicketType` quantity counters (`available + held + sold = total`), not individual seats. This is a deliberate choice: the counter-contention problem (concurrent decrements on a shared count) is a direct version of the flash-sale problem, and it avoids seat-map modeling complexity that doesn't add a new engineering concept at this stage. Assigned seating can be revisited in a future version.
- **Main challenges:**
  - Representing "held," "reserved," "sold" states cleanly on `TicketType` counters
  - Designing for the locking/concurrency strategy used in checkout (this module owns the contended resource)
  - Enforcing the inventory invariant: `available + held + sold = total` at the DB level
- **Sync/Async:** Mostly synchronous reads (browsing). Writes to availability are the contested path — tightly coupled with Checkout module.
- **Status:** Schema complete (V1 + V2 migrations applied). Java domain layer is the next step.

### 2.2 Checkout & Overselling Prevention (★ Primary hard problem)
- **Purpose:** Reserve seats, place orders, guarantee no two people get the same seat under concurrent load.
- **Main challenges:**
  - Choosing and comparing concurrency strategies: optimistic locking (`@Version`), pessimistic locking, or Redis-based seat-hold with TTL
  - Defining a reservation window (hold seat for N minutes before payment expires the hold)
  - Idempotency — handling double-clicks/retries on checkout without double-booking or double-charging
  - Atomicity — decrement inventory + create order + clear hold must be consistent
  - Load-testing multiple approaches and comparing results (this is the comparison story for the resume)
- **Sync/Async:** Reservation itself is synchronous (must be immediate/consistent). Downstream effects (confirmation email, analytics, releasing waiting-room slot) are async.
- **Status:** Not started — this is the centerpiece module, deserves the most design time.

### 2.3 Virtual Waiting Room / Queueing
- **Purpose:** During a flash sale, admit users to checkout in controlled batches instead of letting everyone hit the checkout endpoint simultaneously.
- **Main challenges:**
  - Assigning and communicating queue position (polling vs WebSocket/SSE)
  - Deciding admission rate (fixed batch size? rate per second?)
  - Handling users who abandon the queue (timeout/expiry)
- **Sync/Async:** Queue admission events are naturally async (a worker promotes users from "waiting" to "admitted" on a schedule or as checkout slots free up).
- **Status:** Not started.

### 2.4 Rate Limiting
- **Purpose:** Protect checkout and other hot endpoints from being hammered (bots, refresh-spam) independent of the waiting room.
- **Main challenges:**
  - Choosing an algorithm (token bucket vs sliding window) and justifying the choice
  - Per-user vs per-IP vs per-endpoint limits
  - Where it sits in the request path (filter, interceptor, or gateway layer)
- **Sync/Async:** Synchronous, in the request path. Likely backed by Redis for shared state across instances.
- **Status:** Not started.

### 2.5 Auth & Authorization
- **Purpose:** Account creation/login, and role-based access (buyer vs event organizer vs admin).
- **Main challenges:**
  - JWT vs session-based auth — pick one and justify it
  - Authorization beyond "logged in": can only the event's organizer edit/cancel that event?
  - Keeping this module simple and correct rather than "advanced" — this is table-stakes, not a differentiator
- **Sync/Async:** Synchronous.
- **Status:** Not started.

### 2.6 Payments
- **Purpose:** Handle payment for a reserved order (sandbox/fake provider is fine — e.g., Stripe test mode).
- **Main challenges:**
  - Webhook handling for async payment confirmation
  - Handling failed/declined payments — releasing the seat hold correctly
  - Avoiding double-charging on retries (idempotency keys again, this time on the payment side)
- **Sync/Async:** Payment initiation can be sync; confirmation via webhook is inherently async.
- **Status:** Not started.

### 2.7 Notifications
- **Purpose:** Order confirmation emails, waiting-room admission alerts, payment failure notices.
- **Main challenges:**
  - Decoupling notification sending from the request path so a slow email provider never blocks checkout
  - Retry/failure handling for notification delivery itself
- **Sync/Async:** Fully async — this is the cleanest, most natural home for the async messaging pattern (event published on order placed → notification module consumes it).
- **Status:** Not started.

### 2.8 Observability
- **Purpose:** Prove the architecture holds up under flash-sale load — this is where the "before/after" evidence for the resume comes from.
- **Main challenges:**
  - Metrics: request latency (p50/p95/p99), error rate, queue depth, rate-limiter rejections during a simulated flash sale
  - Logs: structured, with trace IDs correlating a request across modules
  - Tracing: following a single order through Checkout → async Notification/Inventory effects
  - Producing an actual load-test report (k6/Gatling) with dashboards showing system behavior before and after mitigations (rate limiting, queueing, locking strategy)
- **Sync/Async:** N/A — this is cross-cutting infrastructure.
- **Status:** Not started — should be wired in early enough to capture "before" data, not bolted on at the end.

### 2.9 API Gateway *(optional / low priority)*
- **Purpose:** Single entry point, routing, possibly auth offload.
- **Main challenges:** For a single monolith deployment, a gateway doesn't add much real value or a story to tell — likely skip unless a specific use case justifies it (e.g., simulating multiple client types, or routing to a future extracted service).
- **Status:** Deferred / likely cut.

---

## 3. Suggested Build Order (Milestones)

1. ~~**System design sketch**~~ ✅ — entities, the reservation flow, where rate limiter/queue sit. Schema implemented via V1 + V2 Flyway migrations.
2. **Catalog & Inventory** — Java domain layer (entities, repositories, service + REST endpoints). Schema is complete; next conversation.
3. **Checkout & Overselling Prevention** — the centerpiece; implement + benchmark at least two strategies
4. **Auth & Authorization** — keep it simple, get it correct
5. **Rate Limiting** — in front of checkout
6. **Virtual Waiting Room** — controlled admission during simulated flash sale
7. **Payments** — sandbox integration, webhook handling
8. **Notifications** — first real use of async messaging between modules
9. **Observability** — ideally instrumented early enough to capture true before/after data during load tests
10. **(Optional) API Gateway** — only if a clear justification emerges

---

## 4. Open Questions (to resolve per-module, in their own conversations)

- Checkout: optimistic locking vs Redis seat-hold — which to build first, which to compare against?
- Waiting room: polling vs SSE/WebSocket for queue position updates?
- Async backbone: in-process Spring events (Spring Modulith) only, or introduce RabbitMQ, and for which flows specifically?
- Load testing: k6 or Gatling — and what does a "flash sale" simulation script actually look like?
- Observability: self-hosted (Prometheus/Grafana/Loki) vs something simpler, given this is a portfolio project, not production?
