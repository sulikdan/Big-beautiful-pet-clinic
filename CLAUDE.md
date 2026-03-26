# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**Big-beautiful-pet-clinic** — a pet clinic management application with a Java Spring Boot backend and Angular 17 frontend.

Remote: `git@github.com:sulikdan/Big-beautiful-pet-clinic.git`

---

## Backend (Java / Spring Boot)

Located in `backend/`. Requires Java 25 and Maven.

```bash
cd backend
./mvnw spring-boot:run          # start dev server on :8080
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName    # run a single test class
./mvnw package                  # build JAR
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:petclinic`).

### Backend architecture

```
com.petclinic/
  config/       CorsConfig (allows localhost:4200)
  model/        JPA entities: Owner, Animal, Visit, Note + enums Species, Gender
  dto/          Request/response DTOs: OwnerDto, AnimalDto, VisitDto, NoteDto
  repository/   Spring Data JPA repositories (custom search query on AnimalRepository)
  service/      Business logic + entity↔DTO mapping
  controller/   REST controllers + GlobalExceptionHandler
```

**Data model:**
- `Owner` → has many `Animal`s
- `Animal` → belongs to `Owner`; has many `Visit`s and `Note`s
- `Visit` — clinic visit record with `height (cm)`, `weight (kg)`, `age (yrs)`, `vetName`, `diagnosis`, `treatment`
- `Note` — free-text note with auto-set `createdAt`

**Key API endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/animals?name=&species=&ownerId=` | Search/filter animals |
| GET/POST | `/api/animals/{id}` | Get or create animal |
| PUT/DELETE | `/api/animals/{id}` | Update or delete |
| GET/POST | `/api/animals/{id}/visits` | List or add visits |
| PUT/DELETE | `/api/visits/{id}` | Update or delete visit |
| GET/POST | `/api/animals/{id}/notes` | List or add notes |
| DELETE | `/api/notes/{id}` | Delete note |
| GET/POST/PUT/DELETE | `/api/owners` / `/api/owners/{id}` | CRUD owners |

Seed data is loaded from `src/main/resources/data.sql` on startup (3 owners, 5 animals, 5 visits, 5 notes).

---

## Frontend (Angular 21)

Located in `frontend/`. Requires Node 20+ and npm.

```bash
cd frontend
npm install
npm start         # dev server on :4200
npm run build     # production build
npm test          # Karma unit tests
```

### Frontend architecture

Standalone components (no NgModule). Lazy-loaded routes. Angular Material. **Zoneless** change detection (`provideZonelessChangeDetection()`). Signals-first state management.

```
src/app/
  app.config.ts          Bootstrap config — provideZonelessChangeDetection, router, HttpClient
  app.routes.ts          Top-level lazy routes
  models/                TypeScript interfaces: Animal, Owner, Visit, Note
  services/              HTTP services: AnimalService, OwnerService, VisitService, NoteService
  features/
    animals/
      animal-list/       Signals-based list — nameFilter/speciesFilter signals → toSignal(switchMap)
      animal-detail/     animal/visits/notes all toSignal; refresh triggers as signal(0) incremented on mutations
      animal-form/       Reactive form; isEdit = computed(() => !!animalId()); owners = toSignal(http)
      visit-form-dialog/ MatDialog; isEdit = signal; inject() for MAT_DIALOG_DATA
    owners/
      owner-list/        Same signal pattern as animal-list
      owner-form/        Reactive form with inject() DI
  shared/
    confirmation-dialog/ Reusable delete-confirmation MatDialog; inject() for MAT_DIALOG_DATA
```

### Signal patterns used

| Pattern | Usage |
|---------|-------|
| `signal()` | Mutable local state (filters, refresh counters) |
| `computed()` | Derived state (`isEdit`, combined filter objects) |
| `toSignal()` | Wrap HTTP observables into read-only signals |
| `toObservable()` | Convert a signal to an observable for `debounceTime`/`switchMap` |
| `inject()` | All dependency injection (no constructor params) |
| `@if` / `@for` / `@empty` | New control flow — no `NgIf`/`NgFor` imports needed |

All HTTP calls target `http://localhost:8080/api`. The backend must be running for the frontend to work.
