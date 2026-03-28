
# Big-beautiful-pet-clinic

A full-stack pet clinic management application.

## Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 3.6, Spring Data JPA, H2 (dev), PostgreSQL (tests) |
| Frontend | Angular 21, Angular Material, Signals, Zoneless |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers (PostgreSQL) |

## Features

- **Animals** — list, search by name, filter by species, add, edit, delete
- **Visits** — per-animal clinic visit records (date, reason, age, weight, height, vet, diagnosis, treatment)
- **Notes** — free-text notes attached to an animal
- **Owners** — manage owners separately; assign animals to owners

## Getting started

### Backend

Requires Java 25 and Maven.

```bash
cd backend
./mvnw spring-boot:run   # API on :8080
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:petclinic`).
The database is seeded with sample owners, animals, visits, and notes on every startup.

### Frontend

Requires Node 20+ and npm.

```bash
cd frontend
npm install
npm start   # dev server on :4200
```

The backend must be running first. All API calls go to `http://localhost:8080/api`.

## Testing

### Run tests

```bash
cd backend
./mvnw test                       # all tests
./mvnw test -Dtest=ClassName      # single class
```

Integration tests (`*IT`) require Docker to pull the PostgreSQL image on first run.

### Test layers

| Layer | Annotation | Coverage |
|-------|-----------|---------|
| Service unit tests | `@ExtendWith(MockitoExtension.class)` | All methods — happy path, `EntityNotFoundException`, edge cases |
| Controller slice tests | `@WebMvcTest` + `MockMvc` | Status codes, request validation (400/404), JSON structure |
| Repository integration tests | `@DataJpaTest` + Testcontainers | Custom JPQL queries against real PostgreSQL |
| Full-stack integration tests | `@SpringBootTest` + Testcontainers | Owner → Animal → Visit → Note full HTTP lifecycle |

Integration tests activate the `integration` profile, which disables the seed `data.sql` so each test starts with a clean schema.

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/animals?name=&species=&ownerId=` | Search / filter animals |
| GET, POST | `/api/animals` | List all or create |
| GET, PUT, DELETE | `/api/animals/{id}` | Get, update, or delete |
| GET, POST | `/api/animals/{id}/visits` | List or add visits |
| GET, PUT, DELETE | `/api/visits/{id}` | Get, update, or delete a visit |
| GET, POST | `/api/animals/{id}/notes` | List or add notes |
| DELETE | `/api/notes/{id}` | Delete a note |
| GET, POST | `/api/owners` | List or create owners |
| GET, PUT, DELETE | `/api/owners/{id}` | Get, update, or delete an owner |
