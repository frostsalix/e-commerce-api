# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build
./mvnw clean package

# Run (connects to local MySQL at 127.0.0.1:3306/ecommerce)
./mvnw spring-boot:run

# Skip tests during build
./mvnw clean package -DskipTests
```

Java 21, Spring Boot 4.0.6, Maven wrapper. MySQL database `ecommerce` — schema auto-managed by Hibernate (`ddl-auto=update`).

## Architecture

Package layout: `controller` → `service` → `repository` → `model`. Response format: `ApiResponse<T>` with `code`, `message`, `data` fields. HTTP status is always 200 — errors are signalled via the JSON `code` field (400 for validation, 500 for runtime).

**Authentication:** Stateless JWT. `JwtFilter` extracts token from `Authorization: Bearer` header, parses username/role from claims, sets `SecurityContextHolder`. No database lookup per request. Secret key hardcoded in `JwtUtil`. Passwords BCrypt-encoded.

**Authorization:** `ADMIN` role required for product write endpoints. `USER` or `ADMIN` for product reads. Everything else requires authentication except `/users/login` and `/users/register`.

## Key Design Decisions

### Stock deduction happens at payment, not order creation
`OrderService.createOrder()` validates sufficient stock exists but does NOT deduct. `PaymentServiceImpl.success()` performs the actual deduction using `ProductRepository.findByIdForUpdate()` — a `SELECT ... FOR UPDATE` with `@Lock(PESSIMISTIC_WRITE)`. Both methods are `@Transactional`.

### Payment → Order coupling
`Payment.orderId` is a plain `Long`, not a `@ManyToOne` JPA relationship. Payment and Order are decoupled at the ORM level.

### OrderItem is a product snapshot
`OrderItem` stores denormalized `productName` and `productPrice` at order time, plus a `@ManyToOne` back-reference to `Product` for stock deduction lookups.

### Duplicate payment success logic
`PaymentController.success()` has inline business logic that overlaps with `PaymentServiceImpl.success()` but skips stock deduction. The service method is the authoritative path.

### Exception handling
`GlobalExceptionHandler` (a `@RestControllerAdvice`) catches `MethodArgumentNotValidException` → code 400, `RuntimeException` → code 500, `Exception` → code 500 with masked message. Custom `ResourceNotFoundException` exists but is only used in `ProductService` — other services throw plain `RuntimeException`.

## Project Conventions

- Commit messages: Conventional Commits (`feat:`, `refactor:`, `fix:`, `merge:`)
- Lombok: prefer `@Getter`/`@Setter` over `@Data` on entities (JPA entities should not auto-generate `equals`/`hashCode`)
- Responses: always wrap in `ApiResponse.success(data)`, never return bare entities from new endpoints
