# songjaehyun-api

**Interactive Algorithm Demo Engine — Spring Boot Backend**

---

## Overview

This backend powers the interactive algorithm demos for **songjaehyun.com**.

The goal of this service is not simply to expose endpoints — it is to:

- Run **real Java implementations** (not re-written frontend logic)
- Provide per-visitor isolated demo environments
- Log method calls for educational transparency
- Support multiple algorithm demos under one scalable architecture
- Serve as a deployable, production-ready backend (AWS)

This project is designed as both:

- A portfolio demo engine
- A long-term technical blog companion backend
- A cloud-deployable Java service

---

# Core Design Philosophy

### 1. Real Execution Over Simulation

All interactive demos execute real Java domain logic on the server.
The frontend never reimplements the algorithms.

### 2. Clean Separation of Concerns

We strictly separate:

- **Domain logic** (pure Java)
- **HTTP/API layer** (Spring controllers)
- **Platform services** (session + logging infrastructure)

This ensures that:

- Domain logic remains framework-independent
- API layer stays thin
- New demos can be added without rewriting core infrastructure

### 3. Per-Visitor Isolation

Each visitor receives an isolated in-memory instance of a demo using a `sessionId`.

This avoids:

- Shared state pollution
- Multi-user interference
- Persistent storage requirements

---

# High-Level Architecture

```
Frontend (Next.js / static site)
        ↓
HTTPS
        ↓
Spring Boot API
        ↓
Demo Domain Logic (Java)
```

Each demo follows a modular structure inside this backend.

---

# Project Structure

```text
src/main/java/com/songjaehyun/api
│
├── ApiApplication.java
│
├── config/
│   └── CorsConfig.java
│
├── shared/
│   ├── session/
│   │   └── SessionRegistry.java
│   │
│   └── log/
│       ├── DemoLogService.java
│       └── LogEvent.java
│
├── demos/
│   └── expiringkv/
│       ├── domain/
│       │   └── ExpiringKeyValueStore.java
│       │
│       ├── api/
│       │   ├── ExpiringKvController.java
│       │   └── ExpiringKvSessionService.java
│       │
│       └── dto/
│           ├── PutRequest.java
│           ├── PutAtRequest.java
│           ├── SnapshotResponse.java
│           ├── MethodResponse.java
│           └── ValueResponse.java
```

---

# Layer Breakdown

## 1. Domain Layer (`demos/<demo>/domain`)

Contains pure Java implementations.

Characteristics:

- No Spring annotations
- No HTTP knowledge
- Thread-safe where necessary
- Fully testable independently

Example:

- `ExpiringKeyValueStore.java`

This is where algorithmic integrity lives.

---

## 2. API Layer (`demos/<demo>/api`)

Spring controllers map HTTP requests to domain calls.

Responsibilities:

- Validate inputs
- Retrieve session-specific demo instance
- Call domain methods
- Emit log events
- Return structured JSON responses

Controllers are intentionally thin.

---

## 3. DTO Layer (`demos/<demo>/dto`)

Defines request and response shapes.

Why separate DTOs?

- Prevent leaking internal domain types
- Provide stable JSON contract
- Decouple API from internal implementation

---

## 4. Shared Platform Layer (`shared/`)

Reusable infrastructure for all demos.

### SessionRegistry

Tracks:

- `sessionId → lastSeenTimestamp`

Purpose:

- Enables session cleanup
- Prevents unbounded memory growth

### DemoLogService

Stores:

- Per-session event logs
- Capped event history (default 300 events)

Used for:

- Interactive "method call log" UI
- Educational transparency
- Debugging

---

# Logging Model

Each API call generates a structured `LogEvent`:

```java
public record LogEvent(
    long tsMillis,
    String demo,
    String method,
    Map<String, Object> args,
    Object result
)
```

### Field Meanings

| Field      | Purpose                               |
| ---------- | ------------------------------------- |
| `tsMillis` | Server timestamp (epoch millis)       |
| `demo`     | Demo identifier (e.g., "expiring-kv") |
| `method`   | Method invoked                        |
| `args`     | Method parameters                     |
| `result`   | Method output                         |

This allows the frontend to render a real-time execution timeline.

---

# Session Isolation Model

Frontend generates a UUID:

```
crypto.randomUUID()
```

Every request includes:

```
/demos/<demoName>/{sessionId}/...
```

Backend maps:

```
sessionId → ExpiringKeyValueStore instance
```

This gives:

- Isolated demo state
- No authentication required
- Stateless API server (no database)

---

# Why In-Memory?

For demo purposes:

- Speed is critical
- Persistence is not required
- Isolation is more important than durability

Future option:

- Redis-backed state
- RDS-backed persistence
- Distributed sessions

---

# Memory Management Strategy

To avoid memory leaks:

1. Log events are capped (default 300).
2. SessionRegistry tracks last activity.
3. Future scheduled cleanup job will remove sessions inactive for N minutes.

---

# Adding New Demos

To add a new algorithm demo:

1. Create new folder under:

```
demos/<newDemoName>/
```

2. Add:

- `domain/` (pure Java implementation)
- `api/` (controller + session service)
- `dto/` (request/response models)

3. Register endpoints under:

```
/demos/<newDemoName>/{sid}/...
```

No shared infrastructure changes required.

---

# Deployment Strategy

This service is designed to be:

- Packaged as a Docker image
- Pushed to AWS ECR
- Deployed via AWS App Runner or ECS Fargate
- Exposed as:

```
https://api.songjaehyun.com
```

SSL via ACM.
DNS via Route53.

---

# Why This Architecture Scales

This structure supports:

- Multiple algorithm demos
- Interactive educational tools
- Future blog integration
- Potential rate limiting or analytics
- Horizontal scaling (stateless container)

It keeps:

- Domain logic pure
- Infrastructure reusable
- Controllers minimal
- Future expansion clean

---

# Current Implemented Demo

- Expiring Key Value Store (PriorityQueue + HashMap)
  - TTL-based expiration
  - Absolute expiration
  - Session-isolated store
  - Snapshot export
  - Full method logging

---

# Future Roadmap

Planned demos:

- LRU Cache
- Sliding Window Rate Limiter
- Token Bucket
- Bloom Filter
- LFU Cache
- Consistent Hashing Visualizer

Platform upgrades:

- Scheduled session cleanup
- Structured error responses
- Global exception handler
- Observability metrics (Micrometer)
- AWS health checks
- Rate limiting middleware

---

# Why This Backend Exists

This is not a CRUD service.

It is:

- A live algorithm laboratory
- A technical storytelling engine
- A deployable Java service
- A foundation for a technical blog ecosystem

---

# Status

Phase 1 — Bootstrapped
Phase 2 — Platform Layer (Sessions + Logging)
Phase 3 — ExpiringKV Demo
Phase 4 — Docker + AWS Deployment
Phase 5 — Frontend Integration

---
