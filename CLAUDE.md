# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**Big-beautiful-pet-clinic** — a full-stack pet clinic management application.
Spring Boot 3.6 / Java 25 backend · Angular 21 / Signals frontend.

Remote: `git@github.com:sulikdan/Big-beautiful-pet-clinic.git`

---

## Backend (`backend/`)

Requires Java 25 and Maven.

```bash
./mvnw spring-boot:run              # API on :8080
./mvnw test                         # all tests (unit + integration)
./mvnw test -Dtest=ClassName        # single class
./mvnw test -Dtest="*IT"           # integration tests only — requires Docker
./mvnw package                      # build JAR
```

H2 console: `http://localhost:8080/h2-console` · JDBC URL: `jdbc:h2:mem:petclinic`

### Architecture

```
com.petclinic/
  config/       CorsConfig — allows localhost:4200
  model/        JPA entities: Owner, Animal, Visit, Note
                Enums: Species (DOG CAT BIRD RABBIT HAMSTER REPTILE FISH OTHER), Gender
  dto/          OwnerDto, AnimalDto, VisitDto, NoteDto — used for all request/response (no entity serialization)
  repository/   Spring Data JPA; AnimalRepository has a custom JPQL search() query
  service/      Business logic + entity↔DTO mapping; all @Transactional(readOnly = true) by default
  controller/   REST controllers + GlobalExceptionHandler (@RestControllerAdvice → 404/400)
```

**Data model:**
- `Owner` → has many `Animal`s
- `Animal` → belongs to one `Owner` (optional); has many `Visit`s and `Note`s
- `Visit` — clinic visit with `height` (cm), `weight` (kg), `age` (yrs), `vetName`, `diagnosis`, `treatment`
- `Note` — free-text note; `createdAt` auto-set via `@PrePersist`

**Seed data** (`src/main/resources/data.sql`): 3 owners · 5 animals · 5 visits · 5 notes — loaded on every startup.

**Key API routes:**

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/animals?name=&species=&ownerId=` | All params optional; JPQL search |
| POST | `/api/animals` | `@NotBlank name`, `@NotNull species` |
| GET / PUT / DELETE | `/api/animals/{id}` | |
| GET / POST | `/api/animals/{id}/visits` | `@NotNull visitDate` required on POST |
| GET / PUT / DELETE | `/api/visits/{id}` | |
| GET / POST | `/api/animals/{id}/notes` | `@NotBlank content` required |
| DELETE | `/api/notes/{id}` | |
| GET / POST | `/api/owners?search=` | search matches first or last name |
| GET / PUT / DELETE | `/api/owners/{id}` | `@Email` validated |

### Tests

```
src/test/java/com/petclinic/
  service/        Unit — @ExtendWith(MockitoExtension.class), all methods covered
  controller/     Slice — @WebMvcTest, MockMvc; covers 200/201/204/400/404
  integration/    Testcontainers PostgreSQL 16-alpine
    AbstractRepositoryIT  — shared @DataJpaTest + @ServiceConnection base
    AnimalRepositoryIT    — all search() filter combinations
    OwnerRepositoryIT     — name search query
    PetClinicIT           — @SpringBootTest full HTTP lifecycle
```

Integration tests use `@ActiveProfiles("integration")` → `src/test/resources/application-integration.properties`
which sets `spring.sql.init.mode=never` (clean schema, no seed data).

---

## Frontend (`frontend/`)

Requires Node 20+ and npm.

```bash
npm install
npm start         # dev server on :4200
npm run build     # production build
npm test          # Karma unit tests
```

All HTTP calls go to `http://localhost:8080/api`.

### Architecture

Angular 21 · standalone components · lazy-loaded routes · Angular Material · **zoneless** (`provideZonelessChangeDetection()`) · no NgModule · no zone.js.

```
src/app/
  app.config.ts          provideZonelessChangeDetection + provideRouter + provideHttpClient
  app.routes.ts          lazy routes: /animals/** and /owners/**
  models/                Animal (+ Species/Gender unions), Owner, Visit, Note
  services/              AnimalService, OwnerService, VisitService, NoteService — plain HttpClient
  features/
    animals/
      animal-list/       nameFilter + speciesFilter signals → toObservable(computed) → debounce → switchMap → toSignal
      animal-detail/     animal via toSignal(switchMap); visits/notes via refresh-counter pattern
      animal-form/       ReactiveFormsModule; isEdit = computed(() => !!animalId()); owners = toSignal(http$)
      visit-form-dialog/ MatDialog; isEdit = signal(false) set in ngOnInit; inject(MAT_DIALOG_DATA)
    owners/
      owner-list/        same filter → signal pattern as animal-list
      owner-form/        ReactiveFormsModule; inject() DI
  shared/
    confirmation-dialog/ inline template; inject(MAT_DIALOG_DATA) + inject(MatDialogRef)
```

### Signal conventions

| Pattern | Where used |
|---------|-----------|
| `signal()` | Filter values, refresh counters (`signal(0)` incremented after mutations) |
| `computed()` | `isEdit`, combined filter object passed to `toObservable()` |
| `toSignal(obs$)` | All HTTP responses; initial value `[]` or `undefined` |
| `toObservable(sig)` | Bridge signals → RxJS for `debounceTime` / `switchMap` |
| `inject()` | All DI — no constructor parameters anywhere |
| `@if` / `@for` / `@empty` | All templates — `NgIf` / `NgFor` not imported |

**Refresh pattern** (used in `animal-detail`):
```ts
private visitRefresh = signal(0);
visits = toSignal(
  toObservable(computed(() => ({ id: this.animalId(), r: this.visitRefresh() }))).pipe(
    filter(({ id }) => !!id),
    switchMap(({ id }) => this.visitService.getByAnimal(id!))
  ),
  { initialValue: [] as Visit[] }
);
// After mutation:
this.visitRefresh.update(v => v + 1);
```
