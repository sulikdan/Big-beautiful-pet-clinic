# Big-beautiful-pet-clinic

A full-stack pet clinic management application.

## Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 3.6, Spring Data JPA, H2 |
| Frontend | Angular 21, Angular Material, Signals |

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
./mvnw spring-boot:run
```

API runs on `http://localhost:8080`. H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:petclinic`).

The database is seeded with sample owners, animals, visits, and notes on startup.

### Frontend

Requires Node 20+ and npm.

```bash
cd frontend
npm install
npm start
```

App runs on `http://localhost:4200`. The backend must be running first.

## API overview

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/animals?name=&species=&ownerId=` | Search / filter animals |
| GET/POST | `/api/animals` | List or create |
| GET/PUT/DELETE | `/api/animals/{id}` | Get, update, or delete |
| GET/POST | `/api/animals/{id}/visits` | List or add visits |
| GET/PUT/DELETE | `/api/visits/{id}` | Get, update, or delete visit |
| GET/POST | `/api/animals/{id}/notes` | List or add notes |
| DELETE | `/api/notes/{id}` | Delete note |
| GET/POST | `/api/owners` | List or create owners |
| GET/PUT/DELETE | `/api/owners/{id}` | Get, update, or delete owner |
