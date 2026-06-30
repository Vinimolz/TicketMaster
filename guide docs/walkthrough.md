# Docker + PostgreSQL + Spring Boot: Debugging Session

## Overview

This documents the issues encountered while setting up the `payments` and `demo` Spring Boot projects to connect to a Dockerized PostgreSQL database, and how each was resolved.

---

## Problem 1 — `demo` & `payments`: Wrong PostgreSQL instance (port conflict)

### Symptom
```
FATAL: database "demo" does not exist
FATAL: database "payments" does not exist
```

Spring Boot would start but immediately fail to connect, even though Docker was running and the database had been verified to exist via `docker exec`.

### Root Cause

A **native PostgreSQL installation** was already running on the machine as a Windows Service (`postgresql-x64-18`), automatically started on boot. Both it and the Docker container were bound to port `5432`, causing a conflict.

```
netstat -ano | findstr :5432
→ TCP  0.0.0.0:5432  LISTENING  6996   ← local PostgreSQL (postgres.exe)
→ TCP  0.0.0.0:5432  LISTENING  2764   ← Docker Desktop
```

When Spring Boot ran locally via `./mvnw spring-boot:run`, it connected to `localhost:5432` — which resolved to the **local PostgreSQL**, not the Docker container. The local instance had no `demo` or `payments` database, hence the error.

### Fix

Changed Docker to expose PostgreSQL on port `5433` on the host side (container still uses `5432` internally):

**`compose.yaml`** (both projects):
```diff
 ports:
-  - "${DEMO_DB_PORT}:5432"
+  - "5433:5432"
```

**`application.properties`** (both projects):
```diff
-spring.datasource.url=${DEMO_DB_URL:jdbc:postgresql://localhost:5432/demo}
+spring.datasource.url=${DEMO_DB_URL:jdbc:postgresql://localhost:5433/demo}
```

The local PostgreSQL was also set to **Manual** startup so it no longer auto-starts on boot and won't conflict with Docker in the future:

```powershell
# Run as Administrator
Set-Service -Name "postgresql-x64-18" -StartupType Manual
Stop-Service -Name "postgresql-x64-18"
```

---

## Problem 2 — `payments`: Authentication error

### Symptom
```
FATAL: password authentication failed for user "postgres"
```

Even after fixing the port, payments would fail with an auth error.

### Root Cause

Spring Boot **does not read `.env` files**. The `${VAR:default}` syntax in `application.properties` means:
> "Use the OS environment variable `VAR` if set, otherwise fall back to `default`."

When running locally via `./mvnw spring-boot:run`, no OS environment variables are injected — so Spring Boot always uses the **fallback defaults**. The default password in `application.properties` was `postgres`, but the Docker container was initialized with `vini1002`:

| Source | Password |
|--------|----------|
| `.env` → Docker container | `vini1002` |
| `application.properties` default | `postgres` ← **mismatch!** |

### Fix

Updated the fallback default in [application.properties](file:///c:/Users/Vinicius%20Molz/payments/payments/src/main/resources/application.properties) to match the actual credential:

```diff
-spring.datasource.password=${PAYMENTS_DB_PASSWORD:postgres}
+spring.datasource.password=${PAYMENTS_DB_PASSWORD:vini1002}
```

---

## Key Takeaway

> **The fallback defaults in `application.properties` must exactly match the credentials in `.env`.**

Spring Boot running locally never sees the `.env` file — it always falls back to the defaults. The `.env` file is only consumed by Docker Compose to configure the container.

```
./mvnw spring-boot:run  →  reads application.properties defaults only
docker compose up       →  reads .env, injects vars into the container
```

---

## Final Working Configuration

### `payments` project

| Setting | Value |
|---|---|
| Docker host port | `5433` |
| Spring Boot connects to | `localhost:5433/payments` |
| Username | `postgres` |
| Password | `vini1002` |

### `demo` project

| Setting | Value |
|---|---|
| Docker host port | `5433` |
| Spring Boot connects to | `localhost:5433/demo` |
| Username | `postgres` |
| Password | `vini1002` |

### Local PostgreSQL service

| Setting | Value |
|---|---|
| Service name | `postgresql-x64-18` |
| Startup type | `Manual` (no longer auto-starts) |
| Port | `5432` (available for Docker if needed in the future) |
