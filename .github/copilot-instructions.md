# Copilot Instructions for `eco-web-api`

## Build, test, and run commands

- **Build jar:** `.\mvnw.cmd clean package`
- **Build without tests:** `.\mvnw.cmd clean package -DskipTests`
- **Run app locally:** `.\mvnw.cmd spring-boot:run`
- **Run all tests:** `.\mvnw.cmd test`
- **Run a single test class:** `.\mvnw.cmd -Dtest=EcoWebApiApplicationTests test`
- **Run a single test method:** `.\mvnw.cmd -Dtest=EcoWebApiApplicationTests#contextLoads test`

Notes:
- Project targets **Java 21** (`pom.xml` uses `<java.version>21</java.version>`).
- Runtime now expects environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ALIPAY_NOTIFY_SIGN`.
- For local MySQL workflow, point `DB_URL` to `jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=UTC`.
- There is currently **no dedicated lint/checkstyle/spotbugs command** configured in `pom.xml`.

## High-level architecture

- Layering is `controller -> service -> repository -> model`, with DTOs for external payload shaping where implemented.
- API responses are standardized via `ApiResponse<T> { code, message, data }`; errors are generally surfaced via JSON `code` rather than non-200 HTTP status responses from controllers/advice.
- Authentication is stateless JWT:
  - `UserService.login` issues token with username + role claims.
  - `JwtFilter` reads `Authorization: Bearer ...`, builds Spring Security authentication, and sets `SecurityContextHolder`.
  - `SecurityConfig` grants anonymous access only to `/users/login` and `/users/register`; product write APIs require `ADMIN`, product reads require `USER` or `ADMIN`, and other endpoints require authentication.
- Order/payment flow is intentionally split:
  - `OrderService.createOrder` validates stock and creates order + order items, but does **not** deduct stock.
  - `PaymentServiceImpl.success` performs stock deduction and payment/order status transitions inside a transaction using pessimistic row locking (`ProductRepository.findByIdForUpdate`).
  - `OrderService.cancelOrder` restores stock only when cancelling a `PAID` order.

## Key project-specific conventions

- Prefer returning `ApiResponse.success(...)` from controllers (avoid bare entity returns for new endpoints).
- Business logic that depends on current user identity reads username from `SecurityContextHolder`, then resolves the `User` via repository.
- Runtime failures are usually thrown as `RuntimeException` with user-facing messages; `GlobalExceptionHandler` maps validation/runtime/other exceptions into `ApiResponse` error bodies.
- Domain modeling conventions used by order/payment:
  - `Payment` stores `orderId` as a scalar (`Long`) rather than a JPA relation.
  - `OrderItem` stores snapshot fields (`productName`, `productPrice`, `quantity`) and also keeps a `Product` reference for stock operations.
- In this repository, `PaymentServiceImpl.success(...)` is the authoritative payment-success path (stock deduction + status transitions).

## Environment and contribution focus

- Primary local workflow is MySQL-backed development (existing app config points to local MySQL `ecommerce`).
- Docker/ECS work is planned later; prioritize roadmap features first.

## Development roadmap execution

- Follow `DEVELOPMENT_PLAN.md` in order and deliver changes phase-by-phase.
- Do not skip ahead to containerization/infrastructure work unless the active roadmap phase requires it.
- For each roadmap item, finish implementation plus matching endpoint/service/repository wiring before moving to the next item.

## MCP server preferences for this repo

- Prefer database-oriented MCP servers first (MySQL/SQL) for schema, data checks, and query validation tasks.
- For infrastructure/deployment work, prefer MCP servers that help with Docker image workflows and AWS ECS task/service configuration.
