# Gultie Code - Backend

The core REST API and business logic backend for **Gultie Code**, built with **Spring Boot 3** and **Java 17**.

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.2.2
- **Language**: Java 17+ (compatible with modern JDKs)
- **Security**: Spring Security 6 + Stateless JWT (HMAC-SHA512)
- **Database**: PostgreSQL (Supabase / local) / In-Memory H2 fallback
- **ORM / Persistence**: Spring Data JPA / Hibernate
- **Build Tool**: Apache Maven
- **Documentation**: Springdoc OpenAPI / Swagger UI (`/swagger-ui.html`)

---

## 🚀 Key Features

- **OAuth & JWT Session Exchange**: Secure Supabase OAuth token verification and local JWT generation.
- **Role-Based Access Control (RBAC)**: Fine-grained permissions (`ROLE_USER`, `ROLE_EDITOR`, `ROLE_ADMIN`).
- **Course & Level Progression**: Domain management, sequential levels, learning pace indicators, tasks, and point rewards.
- **Assessment Engine**: Dynamic and pre-seeded question evaluation, scoring criteria (80% pass threshold), and completion tracking.
- **Leaderboards & Streak Tracking**: Domain-specific rankings and daily login streak tracking.
- **Notifications & Batch Broadcasts**: Targeted notifications to all users, custom batches, or individual accounts.
- **Admin Audit Logging**: Auditing of administrative and editor actions (role modifications, content deletions, and overrides).

---

## ⚙️ Environment Configuration

Set the following environment variables (or place them in a `.env` file or your runtime environment):

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `SERVER_PORT` | HTTP port | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://<host>:5432/<database>` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `<your-db-password>` |
| `SPRING_JPA_DIALECT` | Hibernate dialect | `org.hibernate.dialect.PostgreSQLDialect` |
| `JWT_SECRET` | 512-bit signing secret | `<64-character-hex-or-string>` |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds | `86400000` (24h) |
| `SUPABASE_URL` | Supabase project URL | `https://<project-ref>.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY`| Supabase service role key | `<service-role-secret>` |
| `AI_API_KEY` | Optional LLM API Key | `<api-key>` |

> **Note**: `.env` and `.env.local` files are ignored by git to protect credentials.

---

## 💻 Getting Started

### 1. Prerequisites
- Java 17+ JDK installed (e.g. OpenJDK 17 or later)
- Apache Maven 3.8+ installed

### 2. Configure Java Home (Windows PowerShell)
```powershell
$env:JAVA_HOME = "C:\path\to\your\jdk"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### 3. Build & Test
```powershell
mvn clean test-compile
```

### 4. Run Locally
```powershell
mvn spring-boot:run
```
The server will start at `http://localhost:8080`.

### 5. Swagger API Documentation
Access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

---

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/gultiecode/
│   │   │   ├── config/          # CORS & application configurations
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/             # Request & Response records / DTOs
│   │   │   ├── entity/          # JPA entities (User, Course, Level, etc.)
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/        # JWT filter, provider, and SecurityConfig
│   │   │   └── service/         # Business logic services
│   │   └── resources/
│   │       ├── application.properties # Spring configuration
│   │       └── schema.sql             # Database schema initialization
└── pom.xml                      # Maven build descriptor
```
