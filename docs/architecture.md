# ProjectMind API — Architecture

This document explains the structural decisions behind the codebase so contributors
can extend it consistently.

## 1. Packaging Strategy: Feature-based

Instead of grouping by technical layer globally (`controllers/`, `services/`, ...),
the code is grouped by **feature**. Each feature module is self-contained:

```
<feature>/
├── controller/   # HTTP boundary only
├── service/      # business logic + transactions
├── repository/   # Spring Data JPA repositories
├── entity/       # JPA entities (+ enums)
├── dto/          # request/response records
└── mapper/       # MapStruct entity <-> dto
```

**Why:** features evolve together. Co-locating them keeps related code in one place,
makes ownership obvious, and avoids the "shotgun surgery" of layer-based packaging.

Shared, cross-cutting code lives in dedicated modules: `config`, `security`, `common`.

## 2. Layering and Responsibilities

- **Controller** — translates HTTP <-> DTO, applies `@Valid`, delegates to a service.
  No business logic. Reads the principal via `@AuthenticationPrincipal`.
- **Service** — owns business rules and transaction boundaries (`@Transactional`).
  Performs ownership checks, throws domain exceptions, maps to DTOs.
- **Repository** — Spring Data JPA. Query methods are scoped by `ownerId` so data
  access is tenant-isolated at the source.

All beans use **constructor injection** (Lombok `@RequiredArgsConstructor`).

## 3. API Contract

Every response is wrapped:

- Success → `ApiResponse<T>` (`success`, `message`, `data`, `timestamp`).
- Error → `ErrorResponse` (`success=false`, `code`, `message`, `path`, `errors`,
  `timestamp`), produced centrally by `GlobalExceptionHandler`.

Domain errors extend `ApiException` and carry an `ErrorCode`, which maps to the HTTP
status. List endpoints return `PageResponse<T>` — a stable projection of Spring Data's
`Page` so the JSON shape never leaks framework internals.

## 4. Persistence

- **`BaseEntity`** provides `id`, audit columns (`createdAt/updatedAt/createdBy/
  updatedBy` via JPA auditing), and `@Version` for **optimistic locking**.
- **Auditing** — `JpaAuditingConfig` wires an `AuditorAware` that reads the current
  username from the security context (falling back to `system`).
- **Soft delete** — `Workspace` and `Tag` use Hibernate `@SoftDelete(columnName =
  "deleted")`; deleted rows are filtered automatically and partial unique indexes keep
  active names unique.
- **Migrations** — Flyway owns the schema (`db/migration/V1__init_schema.sql`). JPA
  runs with `ddl-auto: validate`; the app never mutates schema at runtime.

## 5. Security

- Stateless JWT. `JwtAuthenticationFilter` validates the bearer token and loads the
  principal (`SecurityUserDetails`, which exposes the user id for ownership checks).
- Access tokens are short-lived and signed (HS256). **Refresh tokens are persisted**
  (`refresh_tokens`) so they can be revoked (logout) and rotated (refresh issues a new
  pair and revokes the old one).
- Passwords are hashed with **BCrypt**.
- `SecurityConfig` permits only `/auth/{register,login,refresh}`, health, and Swagger;
  everything else requires authentication. `@EnableMethodSecurity` enables `@PreAuthorize`.

### Authorization (RBAC)

Two roles exist: `USER` (assigned at registration) and `ADMIN`. Domain endpoints are
owner-scoped, so a normal user only ever sees their own data. **Admin** endpoints under
`/api/v1/admin/**` are guarded with class-level `@PreAuthorize("hasRole('ADMIN')")` (method
security, not URL rules) and are intentionally *not* owner-scoped:

- `AdminUserController` — list/view users, enable/disable, and replace roles.
- `AdminAuditLogController` — read every user's audit trail (`AdminAuditLogService`), in
  contrast to the owner-scoped `AuditLogQueryService` behind `/api/v1/audit-logs`.

A non-admin token reaching an admin endpoint passes authentication but fails the
`@PreAuthorize` check, which `GlobalExceptionHandler` maps to a 403 `FORBIDDEN` envelope.
Admin mutations emit audit events (entity type `USER`) like every other mutation.

### Dev seed data

`DevDataSeeder` (`@Profile("dev")`, `CommandLineRunner`) populates demo accounts (`admin`,
`demo`) and sample content on startup so the API and Swagger are immediately usable. It is
idempotent (skips when `admin` exists), inactive under the `test` and production profiles,
and stores no real local model paths.

## 6. Caching

`@EnableCaching` with Redis as the cache manager. `TagService#getById` demonstrates
read-through caching; the cache key is `ownerId:id` so entries are never shared across
users, and writes evict the affected entry.

## 7. Testing

- **Unit tests** (e.g. `WorkspaceServiceTest`) use Mockito with no Spring context.
- **Integration tests** extend `AbstractIntegrationTest`, which boots the full context
  against a real PostgreSQL via **Testcontainers** (`@ServiceConnection`); Flyway runs
  on startup. `AuthControllerIntegrationTest` covers register → login → protected access.

## 8. Domain Modules (Phase 2)

The feature modules all follow the Phase 1 shape and add a few cross-cutting patterns:

- **Ownership & nesting.** Every domain row carries `owner_id` for direct tenant-scoped
  queries. `project`, `task`, `note`, `prompt`, and `resource` also belong to a `workspace`;
  services validate the target workspace (and, for tasks, the optional project) is owned by the
  caller before mutating. `modelregistry` is owner-scoped only (not under a workspace).
- **Dynamic filtering** uses Spring Data JPA `Specification`s (one `*Specifications` helper per
  filterable module). Owner scoping is always the first, non-optional predicate.
- **Cross-entity tagging.** `project` / `note` / `prompt` / `resource` hold a `@ManyToMany`
  to `Tag` via `*_tags` join tables. `TagResolver` centralizes "load these tag ids and verify
  ownership", so the rule lives in one place.
- **Soft-delete fetch constraint.** Because `Workspace`, `Project`, and `Tag` use Hibernate
  `@SoftDelete`, any to-one association pointing at them is mapped `EAGER` (Hibernate rejects
  lazy to-one to a soft-deleted target).

## 9. Audit Logging

Auditing is decoupled via Spring application events. Mutating service methods call
`AuditRecorder.record(...)`, which publishes an `AuditEvent`. `AuditEventListener` handles it
with `@TransactionalEventListener(AFTER_COMMIT)` and `@Transactional(REQUIRES_NEW)`:

- nothing is recorded for transactions that roll back, and
- a failure while writing the audit row cannot roll back the already-committed business change.

`audit_logs` rows are append-only and store the acting `user_id` denormalized (not as an
association). Users can read their own trail via `GET /api/v1/audit-logs`.

## 10. Extending with a New Feature

1. Create the package `com.justin.projectmind.<feature>` with the standard sub-packages.
2. Add a Flyway migration `V{n}__<feature>.sql`.
3. Entity extends `BaseEntity`; reference the owner for tenant isolation.
4. Repository methods take `ownerId`; service enforces ownership and wraps in DTOs.
5. Controller stays thin; annotate with `@SecurityRequirement(name = "bearerAuth")`.
6. Add a unit test for the service and, where it matters, an integration test.
