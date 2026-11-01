# Online Bookstore

A full-stack bookstore kata built with React, Spring Boot 3, Java 17, Spring Data JPA, Spring Security/JWT, H2 (local), and MySQL (production profile).

## Features
- User registration and login with BCrypt password hashing and JWT authentication
- Browse books and view book details
- Persistent per-user shopping cart; add, update quantity, remove, clear
- Transactional checkout with stock validation and stock decrement
- Order history and order details
- DTO-based API, validation, global error handling, pagination, and CORS
- Backend unit/integration tests and frontend component tests

## Prerequisites
- JDK 17+
- Maven 3.9+
- Node.js 20+ and npm
- Optional: MySQL 8+

## Run backend (H2)
```bash
cd backend
mvn clean test
mvn spring-boot:run
```
API: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html  
H2 console (local only): http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:bookstoredb`, user `sa`, blank password.

A development-only data seeder creates sample books and a demo user:
- Email: `demo@bookstore.local`
- Password: `Password123!`

Do not use seeded credentials or H2 console in production.

## Run frontend
```bash
cd frontend
npm install
npm start
```
React: http://localhost:3000  
The frontend proxies API requests to `http://localhost:8080` in development. If you deploy separately, set `REACT_APP_API_URL`.

## MySQL profile
Create a database and user, then set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_URL='jdbc:mysql://localhost:3306/bookstore?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='bookstore'
export DB_PASSWORD='change-me'
export JWT_SECRET='replace-with-a-random-secret-at-least-32-bytes-long'
```
Then run `mvn spring-boot:run`. The MySQL profile uses Flyway migrations. For a real deployment, use a secrets manager, TLS, backups, monitoring, and a managed database.

## API overview
| Method | Endpoint | Auth | Description |
|---|---|---:|---|
| POST | `/api/auth/register` | No | Register |
| POST | `/api/auth/login` | No | Login |
| GET | `/api/books` | No | List books (`page`, `size`, `sort`) |
| GET | `/api/books/{id}` | No | Book details |
| GET | `/api/cart` | Yes | Current cart |
| POST | `/api/cart/items` | Yes | Add `{ "bookId": 1, "quantity": 1 }` |
| PUT | `/api/cart/items/{bookId}` | Yes | Set `{ "quantity": 2 }` |
| DELETE | `/api/cart/items/{bookId}` | Yes | Remove item |
| DELETE | `/api/cart` | Yes | Clear cart |
| POST | `/api/orders/checkout` | Yes | Checkout |
| GET | `/api/orders` | Yes | Current user's orders |
| GET | `/api/orders/{id}` | Yes | Order details |

## TDD and engineering notes
Use Red → Green → Refactor:
1. Write a failing test describing one behavior.
2. Implement the smallest change that makes it pass.
3. Refactor while keeping the test suite green.

The service layer owns business rules; controllers are thin; repositories only persist/query. DTOs prevent persistence entities from becoming public API contracts. `@Transactional` protects checkout as one unit of work. `BigDecimal` is used for money. Cart and order queries are scoped to the authenticated principal.

### Suggested team workflow
- Branch per story: `feature/book-catalog`, `feature/cart`, etc.
- Commit tests with implementation.
- Require `mvn clean verify` and `npm test -- --watchAll=false` in CI.
- Add code review, dependency scanning, formatting, and coverage gates.
- Keep secrets out of source control.

## Scope / production hardening
This is a runnable kata baseline, not a fully audited commerce platform. Before production: add refresh-token rotation/revocation, rate limiting, email verification/password reset, payment-provider integration, idempotency keys, audit logs, structured observability, inventory reservation/expiry, stronger database migration governance, and security testing. Checkout currently records an order; it does not charge a payment method.
