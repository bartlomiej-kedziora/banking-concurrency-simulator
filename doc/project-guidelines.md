# Banking Concurrency Simulator — Project Guidelines

## 1. Purpose

Banking Concurrency Simulator is an educational and portfolio project for exploring concurrency, thread safety, and consistency of banking operations in Java.

The primary goal is to understand and implement concurrency mechanisms directly, rather than delegate them to an application framework. The project should remain small enough to reason about, while demonstrating sound software engineering practices.

This document records the project's agreed technical direction and architectural constraints. Update it when a deliberate design decision changes; keep it consistent with the implementation and the requirements.

## 2. Technology Decisions

| Area | Decision |
| --- | --- |
| Language | Java 25 |
| Build | Maven |
| Application framework | None; no Spring Boot or Spring Framework |
| Dependency injection | Explicit constructor injection and manual object composition |
| Initial persistence | In-memory repository |
| Optional future persistence | H2 using JDBC directly |
| Unit testing | JUnit 5 |
| Mocking | Mockito only when it provides clear value |
| Production dependencies | Minimize external libraries; prefer the JDK |

**Principle:** Prefer plain Java and standard JDK APIs. Introduce a third-party dependency only when its benefits justify the additional complexity.

H2 is a possible learning tool for database transactions and concurrency, not a prerequisite for version 1.

## 3. Architectural Principles

- Apply separation of concerns, SOLID, DRY, KISS, and YAGNI pragmatically.
- Keep the domain model independent of persistence and application frameworks.
- Use constructor injection without a dependency-injection container.
- Depend on interfaces where they define a meaningful boundary or allow a genuine alternative implementation; avoid interfaces added only for ceremony.
- Keep responsibilities and dependency directions explicit.
- Avoid premature abstractions, multi-module builds, and infrastructure not needed by the current version.
- Prefer clear, intention-revealing names and domain terminology.

### Current package responsibilities

| Package | Responsibility |
| --- | --- |
| `domain` | Account state, account identity, and business rules |
| `application.transfer` | Transfer use case and transfer execution strategy |
| `application.port.output` | Repository abstraction used by the application |
| `persistence` | In-memory repository implementation |

### Current components

- `BankAccount`: owns account balance and synchronized balance operations.
- `AccountNumber`: immutable, validated account identifier; comparable for deterministic lock ordering.
- `BankTransferService`: coordinates account lookup and delegates transfer execution.
- `TransferExecutor`: contract for executing a transfer between two accounts.
- `SynchronizedTransferExecutor`: transfer implementation using ordered intrinsic locks.
- `BankAccountRepository`: persistence port.
- `InMemoryBankAccountRepository`: in-memory adapter implementing that port.

These descriptions reflect the current v1 design, not a commitment to preserve every class or interface indefinitely.

## 4. Version 1 Scope

Version 1 focuses on **intrinsic Java synchronization** and in-memory state.

- Use `Thread`, `synchronized`, and `join()` to explore concurrent execution.
- Support account balance operations and transfers without overdrafts.
- Reject zero or negative operation amounts.
- Prevent integer overflow on deposits.
- Prevent concurrent transfers from violating account balance rules.
- Acquire the two account locks in a deterministic order to avoid circular lock acquisition.
- Test successful, rejected, invalid, and concurrent operations.

### Deliberately out of scope for v1

- Spring Boot, Spring Framework, and other application frameworks.
- ORM frameworks such as JPA/Hibernate.
- A relational database or distributed persistence.
- Advanced synchronization primitives such as `ReentrantLock` (reserved for later learning stages).
- Transaction reservations, transaction history, audit logs, idempotency, daily limits, and other future domain features unless separately approved for the release.
- A general rollback or recovery mechanism for partially executed in-memory transfers.

The broader feature ideas in `requirements.md` are not automatically part of v1. Distinguish implemented behavior, committed release scope, and future proposals before treating a requirement as complete.

## 5. Concurrency and Consistency

### Current synchronization model

- `BankAccount` uses synchronized methods to protect mutable balance state.
- `SynchronizedTransferExecutor` locks both account objects in a consistent order determined by `AccountNumber`.
- Withdrawal and deposit feasibility are checked while both account locks are held, before either balance is modified.
- A rejected transfer must leave both balances unchanged.
- The sum of balances must be preserved by successfully executed transfers.

### Important limitations

- `synchronized` provides mutual exclusion and Java Memory Model visibility guarantees; it does **not** provide rollback.
- The current implementation has no recovery mechanism if an unexpected failure occurs after withdrawal but before deposit.
- Pre-validation makes ordinary business-rule rejections safe under the current account implementation, but it is not a general transaction mechanism.
- In-memory locking applies to Java object instances. It must not be assumed to provide database transaction atomicity or consistency across processes.
- Adding JDBC persistence will require a separate design for transaction boundaries, isolation, locking, and failure recovery.

## 6. Testing Strategy

- Use JUnit 5 and behavior-oriented test names.
- Test domain rules independently from application orchestration.
- Test transfer results **and** balance invariants; balance consistency alone does not establish that the execution contract was respected.
- Cover insufficient funds, invalid amounts, integer overflow, invalid account references, and identical account identifiers.
- Cover competing concurrent transfers and transfers in opposite directions.
- When using worker threads in tests, verify that they finish and distinguish a returned `false` from a missing result caused by an unexpected failure.
- `Thread.join(timeout)` does not stop a blocked thread and does not prove that operations actually overlapped. Treat these as known limitations of the current tests.
- A successful `join()` or another confirmed observation of thread termination establishes the relevant happens-before relationship for reading results written by that thread.
- Introduce stronger concurrency-test coordination only when needed to validate a specific property.

## 7. Future Learning Stages

Possible future directions, subject to separate design decisions:

1. Compare intrinsic locks with other JDK concurrency mechanisms, including explicit locks and executor-based execution.
2. Introduce transaction identifiers and idempotency where useful.
3. Explore H2 with plain JDBC, explicit database transactions, and isolation behavior.
4. Compare object-level synchronization with database-level concurrency control.
5. Extend domain features incrementally, guided by tests and documented requirements.

These are possibilities, not an approved implementation schedule.

## 8. Documentation and Repository Rules

- Write source code, identifiers, comments, and documentation in English.
- Keep detailed technical documentation as Markdown in `doc/`.
- Maintain PlantUML diagrams under `doc/diagrams/`.
- `doc/diagrams/domain-model.puml` should show the **implemented** domain model, not unimplemented future entities.
- `doc/diagrams/architecture.puml` should show current component responsibilities and dependencies.
- `doc/diagrams/use-cases.puml` should distinguish implemented use cases from conceptual external actors or planned interactions.
- Keep `README.md`, requirements, diagrams, and implementation consistent.
- Record important changes to architecture and scope in this file; introduce an ADR only when a decision warrants a separate record.

## 9. Decision Log

| Decision | Rationale |
| --- | --- |
| No Spring Boot or Spring Framework | Keep the concurrency mechanisms visible and understandable. |
| Prefer standard JDK APIs | Minimize dependencies and maximize direct learning. |
| Use Maven | Maintain a consistent, simple build workflow. |
| Start with in-memory persistence | Focus v1 on synchronization rather than database concerns. |
| Consider H2 with JDBC later | Learn database transactions without introducing an ORM. |
| Separate domain, application, and persistence | Preserve clear responsibilities and dependency boundaries. |
| Use ordered account locks for transfers | Avoid circular wait when locking two accounts. |
| Keep v1 deliberately limited | Prevent advanced features from obscuring the learning goal. |

## 10. Related Documents

- [Requirements](requirements.md)
- [Domain model](diagrams/domain-model.puml)
- [Use cases](diagrams/use-cases.puml)
- [Architecture diagram](diagrams/architecture.puml) — proposed; add the file when approved and committed.
