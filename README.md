# Recall

**Recall** is a web-based flashcard platform that helps learners retain study material through **spaced repetition**.

The system uses a from-scratch implementation of the **SM-2 (SuperMemo 2) algorithm** to determine when each flashcard should be reviewed again. After reviewing a card, the learner gives a recall-quality rating from **0 to 5**, and Recall calculates the next review date based on that rating.

The main technical focus of the project is the implementation of the scheduling algorithm as a **pure, independently testable component**.

---

## Features

* User registration and login
* JWT-based authentication
* Create, edit, archive, and delete flashcard decks
* Create, edit, and delete flashcards
* Optional hints and tags for cards
* Review cards that are due
* Rate recall quality from 0–5
* Automatic scheduling using the SM-2 algorithm
* Daily study streak tracking
* Dashboard with study statistics
* Review activity information
* Search cards by text
* Organise cards using tags
* PostgreSQL database
* Docker and Docker Compose support
* Automated CI using GitHub Actions
* Public deployment support

---

## Technology Stack

### Frontend

* React 18
* TypeScript
* Vite
* Tailwind CSS
* Axios
* Recharts

### Backend

* Java 21
* Spring Boot 3
* Spring Data JPA
* Spring Security
* JWT
* BCrypt
* Maven
* Flyway

### Database

* PostgreSQL 16

### Testing

* JUnit 5
* Mockito

### Development and Deployment

* Git
* GitHub
* GitHub Actions
* Docker
* Docker Compose
* Render

---

## System Architecture

Recall uses a layered client-server architecture:

```text
┌─────────────────────────┐
│       Frontend          │
│   React + TypeScript    │
└────────────┬────────────┘
             │ REST API
             ▼
┌─────────────────────────┐
│        Backend          │
│ Spring Boot + Java 21   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│       Database          │
│      PostgreSQL 16      │
└─────────────────────────┘
```

The frontend provides the user interface, the Spring Boot backend provides the REST API and application logic, and PostgreSQL stores application data.

Authentication is handled using JWT, with passwords protected using BCrypt.

---

## SM-2 Scheduling

The SM-2 algorithm is the core component of Recall.

After a learner reviews a card, they provide a quality rating between **0 and 5**. The system uses this rating together with the card's previous scheduling information to calculate:

* Ease factor
* Number of repetitions
* Review interval
* Next review date

The scheduling algorithm is implemented as a framework-independent component:

```text
Sm2Algorithm.schedule(...)
```

It receives the required scheduling values and returns a scheduling result without depending on Spring, JPA, or database entities.

This separation makes the scheduling logic easier to test and maintain.

---

## Main Application Pages

The application contains:

* **Login** — user authentication
* **Register** — new user registration
* **Dashboard** — study statistics and activity
* **Decks** — manage flashcard decks
* **Deck Detail** — manage cards within a deck
* **Study** — review cards and submit recall ratings

---

## Database

Recall uses PostgreSQL for persistent storage.

The main entities are:

* User
* Deck
* Card
* Tag
* ReviewLog

Database changes are managed using **Flyway migrations**.

The project uses migrations rather than automatically generating the database schema from the JPA entities. Hibernate uses validation to check that the entity model matches the database schema.

---

## API

The backend provides REST endpoints for:

* Authentication
* Deck management
* Card management
* Reviews
* Dashboard statistics

Protected endpoints require a valid JWT.

The API can also be inspected using the project's API documentation/Swagger interface.

---

## Running the Project with Docker

Docker Compose is provided to simplify local setup.

### Prerequisites

Make sure the following are installed:

* Docker
* Docker Compose
* Git

### Clone the repository

```bash
git clone <https://github.com/kishlaykumar990-hue/recall_a-smart-flashcart-app.git>

```

## 6. Running locally

### Option A — Docker Compose (recommended, full stack)

```bash
cp .env.example .env    # edit JWT_SECRET at minimum
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Postgres: localhost:5432

A demo account is seeded via Flyway: **demo@example.com / Password123!**

### Option B — Run backend and frontend separately

**Backend** (requires JDK 21, Maven, a local PostgreSQL):
```bash
cd backend
createdb flashcard_db   # or use docker: docker run -p 5432:5432 -e POSTGRES_PASSWORD=flashcard_pass postgres:16
mvn spring-boot:run
```

**Frontend** (requires Node 20+):
```bash
cd frontend
npm install
npm run dev
```
Vite's dev server proxies `/api` to `http://localhost:8080` (see `vite.config.ts`).

## 7. Running tests

```bash
# Backend: algorithm unit tests + service tests
cd backend
mvn test

# Frontend
cd frontend
npm run test
```

## Project Structure

A simplified structure is:

```text
flashcard-scheduler/
├── backend/
│   ├── src/main/java/com/flashcard/scheduler/
│   │   ├── config/          # SecurityConfig
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # request/response records
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # custom exceptions + GlobalExceptionHandler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JwtService, JwtAuthenticationFilter
│   │   └── service/
│   │       ├── Sm2Algorithm.java       # <- pure SM-2 engine
│   │       └── impl/                   # orchestration services
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    # Flyway SQL migrations (schema + seed data)
│   ├── src/test/java/...    # Sm2AlgorithmTest, SchedulingServiceTest
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/           # Login, Register, Dashboard, Decks, DeckDetail, Study
│   │   ├── components/      # NavShell, ProtectedRoute
│   │   ├── context/         # AuthContext
│   │   ├── services/        # axios API clients
│   │   ├── types/           # shared TS types mirroring backend DTOs
│   │   └── test/            # Vitest tests
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml
├── .env.example
└── .github/workflows/ci.yml
```

The exact structure may vary depending on the final repository organisation.

---

## Security

Recall uses several security mechanisms:

* JWT authentication
* BCrypt password hashing
* Protected REST endpoints
* Input validation
* Centralised error handling

Passwords are not stored as plaintext.

---

## Deployment

The Docker images are self-contained and can be deployed to any container host:
- **Render / Railway**: point each service at `backend/Dockerfile` and `frontend/Dockerfile`
  respectively, add a managed Postgres add-on, and set `SPRING_DATASOURCE_*` and
  `JWT_SECRET` as environment variables.
- **Any VM / AWS EC2 / Lightsail**: `git clone`, `cp .env.example .env` (edit secrets),
  `docker compose up -d --build`.
- **Kubernetes**: the two Dockerfiles are ordinary multi-stage builds and can be pushed to
  any registry and wrapped in standard Deployment/Service manifests; not included here to
  avoid shipping unused boilerplate, but the container images require no changes.

## Project Scope

The current project focuses on:

* Authentication
* Flashcard and deck management
* Spaced-repetition reviews
* SM-2 scheduling
* Study streaks
* Dashboard statistics
* Search and tags
* Containerised deployment

The following features are outside the current scope:

* Administrative interface
* Notifications
* Dark mode
* Machine-learning-based scheduling

These are documented as possible future improvements rather than part of the current implementation.

---

## Author

**Kishlay Kumar**


