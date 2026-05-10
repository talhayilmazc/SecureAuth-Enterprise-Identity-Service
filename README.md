# SecureAuth — Enterprise Identity & Authorization Service

> Production-grade authentication and authorization microservice built for enterprise systems.
> Java 17 + Spring Boot 3 + Spring Security + JWT + Redis + Docker + CI/CD

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.3-green?logo=springsecurity)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![CI](https://github.com/talhayilmazc/SecureAuth-Enterprise-Identity-Service/actions/workflows/ci.yml/badge.svg)

---

## 🔐 What This System Does

SecureAuth is a production-style identity and authorization microservice.
It models how enterprise systems handle authentication, token management,
brute-force protection, and security audit trails.

This is **not a basic login API** — it implements:

- ✅ JWT access + refresh token lifecycle
- ✅ Brute-force protection with automatic account lockout
- ✅ Role-based access control (RBAC) with 4 distinct roles
- ✅ Token rotation on refresh (revoke old, issue new)
- ✅ Password reset with time-limited tokens
- ✅ Email verification flow
- ✅ Immutable security audit trail for every action
- ✅ Login attempt tracking with IP and user agent
- ✅ Redis token blacklisting and session management
- ✅ Scheduled cleanup of expired tokens
- ✅ OpenAPI/Swagger documentation
- ✅ GitHub Actions CI/CD pipeline
- ✅ Docker Compose ready

---

## 🏗️ Architecture
Client
│
▼
JWT Authentication Filter
│
▼
REST Controllers ──► Service Layer ──► Repository Layer ──► PostgreSQL
│
▼
Redis Cache Layer (token blacklist, session)

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3, Spring MVC, Spring Security |
| Authentication | JWT (jjwt 0.11.5) — access + refresh tokens |
| Database | PostgreSQL 15 + Hibernate/JPA ORM |
| Caching | Redis 7 — token management |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven 3.9 |
| DevOps | Docker, Docker Compose, GitHub Actions CI/CD |
| Testing | JUnit 5, Mockito, AssertJ — 13 unit tests |

---

## 🔐 Security Features

### Brute Force Protection
- Failed login attempts tracked per user
- Account automatically locked after 5 failed attempts
- Lockout duration: 30 minutes
- Login attempts stored with IP address and user agent

### Token Lifecycle
- Access token: 24 hours
- Refresh token: 7 days
- Token rotation on every refresh (old token revoked)
- Full token revocation on password change
- Scheduled cleanup of expired tokens (daily at 02:00)

### Audit Trail
Every security event is logged immutably:
LOGIN, LOGOUT, REGISTER, PASSWORD_CHANGE, PASSWORD_RESET_REQUEST,
PASSWORD_RESET_COMPLETE, EMAIL_VERIFICATION, TOKEN_REFRESH,
TOKEN_REVOKE, ACCOUNT_LOCK, ACCOUNT_UNLOCK, ROLE_CHANGE

---

## 🔑 RBAC — Role Based Access Control

| Role | Capabilities |
|---|---|
| `ROLE_ADMIN` | Full system access, user management |
| `ROLE_MANAGER` | Extended access |
| `ROLE_MODERATOR` | Moderation access |
| `ROLE_USER` | Standard user access |

---

## 📁 Project Structure
src/main/java/com/secureauth/secureauth/
├── config/          # Security, JPA, Redis, OpenAPI configs
├── controller/      # Auth, User, AuditLog REST controllers
├── domain/
│   ├── entity/      # User, RefreshToken, LoginAttempt, AuditLog
│   ├── enums/       # Role, LoginResult, AuditAction
│   └── repository/  # Spring Data JPA repositories
├── dto/
│   ├── request/     # RegisterRequest, LoginRequest, ChangePasswordRequest...
│   └── response/    # AuthResponse, UserResponse, TokenValidationResponse...
├── exception/       # Global exception handling
├── security/        # JWT provider, filter, UserPrincipal
└── service/         # Business logic (interfaces + implementations)

---

## 🚀 Running Locally

### Prerequisites
- Docker Desktop
- Java 17
- Maven 3.9+

### Start all services

```bash
docker compose up -d
```

This starts:
- **App** → http://localhost:8082
- **PostgreSQL** → localhost:5433
- **Redis** → localhost:6380

### API Documentation
http://localhost:8082/swagger-ui/index.html

### Health Check
http://localhost:8082/actuator/health

---

## 🔑 Authentication Flow

### Register
```bash
POST /api/v1/auth/register
{
  "username": "talha",
  "password": "Password1!",
  "email": "talha@example.com",
  "firstName": "Talha",
  "lastName": "Yılmaz"
}
```

### Login
```bash
POST /api/v1/auth/login
{
  "username": "talha",
  "password": "Password1!"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "talha"
}
```

### Refresh Token
```bash
POST /api/v1/auth/refresh
{
  "refreshToken": "550e8400-e29b-41d4-a716..."
}
```

---

## 📊 API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register new user | Public |
| POST | `/api/v1/auth/login` | Login | Public |
| POST | `/api/v1/auth/refresh` | Refresh token | Public |
| POST | `/api/v1/auth/logout` | Logout | Required |
| POST | `/api/v1/auth/logout-all` | Logout all sessions | Required |
| GET | `/api/v1/auth/validate` | Validate token | Public |
| POST | `/api/v1/auth/forgot-password` | Request password reset | Public |
| POST | `/api/v1/auth/reset-password` | Reset password | Public |
| POST | `/api/v1/auth/change-password` | Change password | Required |
| GET | `/api/v1/auth/verify-email` | Verify email | Public |
| GET | `/api/v1/users/me` | Current user | Required |
| GET | `/api/v1/users` | List all users | ADMIN |
| POST | `/api/v1/users/{id}/lock` | Lock account | ADMIN |
| POST | `/api/v1/users/{id}/unlock` | Unlock account | ADMIN |
| GET | `/api/v1/audit-logs/user/{username}` | User audit trail | ADMIN |

---

## 🧪 Testing

```bash
mvn test
```

- ✅ AuthServiceTest — 7 unit tests (register, login, lockout, bad credentials)
- ✅ TokenServiceTest — 5 unit tests (create, validate, revoke, expiry)
- ✅ SecureauthApplicationTests — context loads

---

## 🔄 CI/CD Pipeline

GitHub Actions pipeline on every push:

1. **Build** — `mvn clean compile`
2. **Test** — `mvn test` with PostgreSQL + Redis
3. **Docker Build** — builds image on `main` and `develop`

---

## 👨‍💻 Author

**Talha Yılmaz**
[github.com/talhayilmazc](https://github.com/talhayilmazc) · [linkedin.com/in/talha-yilmaz-38a13a225](https://linkedin.com/in/talha-yilmaz-38a13a225)