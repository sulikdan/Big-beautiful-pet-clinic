package com.petclinic.integration;

import com.petclinic.dto.AnimalDto;
import com.petclinic.dto.NoteDto;
import com.petclinic.dto.OwnerDto;
import com.petclinic.dto.VisitDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration tests using a real PostgreSQL container.
 * Covers the complete HTTP request → service → repository → database flow.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration")
class PetClinicIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    // ── Owner CRUD ────────────────────────────────────────────────────────────

    @Test
    void createAndRetrieveOwner() {
        OwnerDto dto = ownerPayload("Jane", "Doe", "jane@example.com");

        ResponseEntity<OwnerDto> created = rest.postForEntity("/api/owners", dto, OwnerDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().getId();
        assertThat(id).isNotNull();

        ResponseEntity<OwnerDto> fetched = rest.getForEntity("/api/owners/" + id, OwnerDto.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void ownerNotFound_returns404() {
        ResponseEntity<String> response = rest.getForEntity("/api/owners/99999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOwner_missingFirstName_returns400() {
        OwnerDto dto = new OwnerDto();
        dto.setLastName("NoFirst");
        ResponseEntity<String> response = rest.postForEntity("/api/owners", dto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void searchOwners_byName() {
        rest.postForEntity("/api/owners", ownerPayload("SearchFirst", "SearchLast", null), OwnerDto.class);

        OwnerDto[] result = rest.getForObject("/api/owners?search=SearchFirst", OwnerDto[].class);
        assertThat(result).anyMatch(o -> o.getFirstName().equals("SearchFirst"));
    }

    // ── Animal CRUD ───────────────────────────────────────────────────────────

    @Test
    void createAndDeleteAnimal() {
        OwnerDto owner = rest.postForEntity("/api/owners", ownerPayload("Del", "Owner", null), OwnerDto.class).getBody();

        AnimalDto dto = animalPayload("Doggo", "DOG", owner.getId());
        ResponseEntity<AnimalDto> created = rest.postForEntity("/api/animals", dto, AnimalDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long animalId = created.getBody().getId();
        assertThat(created.getBody().getOwnerName()).contains("Del");

        rest.delete("/api/animals/" + animalId);
        ResponseEntity<String> gone = rest.getForEntity("/api/animals/" + animalId, String.class);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void searchAnimals_byName_returnsMatch() {
        rest.postForEntity("/api/animals", animalPayload("UniqueAnimalXYZ", "RABBIT", null), AnimalDto.class);

        AnimalDto[] result = rest.getForObject("/api/animals?name=UniqueAnimalXYZ", AnimalDto[].class);
        assertThat(result).anyMatch(a -> a.getName().equals("UniqueAnimalXYZ"));
    }

    @Test
    void searchAnimals_bySpecies_returnsOnlyThatSpecies() {
        rest.postForEntity("/api/animals", animalPayload("FishyFish", "FISH", null), AnimalDto.class);

        AnimalDto[] result = rest.getForObject("/api/animals?species=FISH", AnimalDto[].class);
        assertThat(result).allMatch(a -> "FISH".equals(a.getSpecies()));
    }

    @Test
    void createAnimal_invalidBody_returns400() {
        AnimalDto invalid = new AnimalDto(); // missing name and species
        ResponseEntity<String> response = rest.postForEntity("/api/animals", invalid, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Visit CRUD ────────────────────────────────────────────────────────────

    @Test
    void addAndRetrieveVisit() {
        AnimalDto animal = rest.postForEntity("/api/animals", animalPayload("VisitPet", "CAT", null), AnimalDto.class).getBody();

        VisitDto visit = visitPayload(LocalDate.of(2024, 6, 1), "Check-up", 4.5, 30.0, 65.0);
        ResponseEntity<VisitDto> created = rest.postForEntity("/api/animals/" + animal.getId() + "/visits", visit, VisitDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().getVetName()).isEqualTo("Dr. Test");
        assertThat(created.getBody().getWeight()).isEqualTo(30.0);

        VisitDto[] visits = rest.getForObject("/api/animals/" + animal.getId() + "/visits", VisitDto[].class);
        assertThat(visits).hasSize(1);
        assertThat(visits[0].getReason()).isEqualTo("Check-up");
    }

    @Test
    void updateVisit_changesFields() {
        AnimalDto animal = rest.postForEntity("/api/animals", animalPayload("UpdatePet", "DOG", null), AnimalDto.class).getBody();
        VisitDto created = rest.postForEntity("/api/animals/" + animal.getId() + "/visits",
                visitPayload(LocalDate.of(2024, 1, 1), "Initial", 2.0, 25.0, 50.0), VisitDto.class).getBody();

        created.setReason("Follow-up");
        created.setDiagnosis("Recovered");
        rest.put("/api/visits/" + created.getId(), created);

        VisitDto updated = rest.getForObject("/api/visits/" + created.getId(), VisitDto.class);
        assertThat(updated.getReason()).isEqualTo("Follow-up");
        assertThat(updated.getDiagnosis()).isEqualTo("Recovered");
    }

    @Test
    void deleteVisit_removesFromAnimal() {
        AnimalDto animal = rest.postForEntity("/api/animals", animalPayload("DelVisitPet", "CAT", null), AnimalDto.class).getBody();
        VisitDto visit = rest.postForEntity("/api/animals/" + animal.getId() + "/visits",
                visitPayload(LocalDate.of(2024, 3, 1), "To delete", 1.0, 10.0, 20.0), VisitDto.class).getBody();

        rest.delete("/api/visits/" + visit.getId());

        VisitDto[] remaining = rest.getForObject("/api/animals/" + animal.getId() + "/visits", VisitDto[].class);
        assertThat(remaining).isEmpty();
    }

    // ── Note CRUD ─────────────────────────────────────────────────────────────

    @Test
    void addAndDeleteNote() {
        AnimalDto animal = rest.postForEntity("/api/animals", animalPayload("NotePet", "DOG", null), AnimalDto.class).getBody();

        NoteDto note = new NoteDto();
        note.setContent("First note");
        NoteDto created = rest.postForEntity("/api/animals/" + animal.getId() + "/notes", note, NoteDto.class).getBody();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();

        NoteDto[] notes = rest.getForObject("/api/animals/" + animal.getId() + "/notes", NoteDto[].class);
        assertThat(notes).hasSize(1);

        rest.delete("/api/notes/" + created.getId());
        NoteDto[] afterDelete = rest.getForObject("/api/animals/" + animal.getId() + "/notes", NoteDto[].class);
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void multipleNotes_returnedNewestFirst() {
        AnimalDto animal = rest.postForEntity("/api/animals", animalPayload("MultiNotePet", "CAT", null), AnimalDto.class).getBody();

        NoteDto n1 = new NoteDto();
        n1.setContent("First note");
        rest.postForEntity("/api/animals/" + animal.getId() + "/notes", n1, NoteDto.class);

        NoteDto n2 = new NoteDto();
        n2.setContent("Second note");
        rest.postForEntity("/api/animals/" + animal.getId() + "/notes", n2, NoteDto.class);

        NoteDto[] notes = rest.getForObject("/api/animals/" + animal.getId() + "/notes", NoteDto[].class);
        assertThat(notes).hasSize(2);
        // Repository orders by createdAt DESC — second note should be first
        assertThat(notes[0].getContent()).isEqualTo("Second note");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static OwnerDto ownerPayload(String first, String last, String email) {
        OwnerDto dto = new OwnerDto();
        dto.setFirstName(first);
        dto.setLastName(last);
        dto.setEmail(email);
        return dto;
    }

    private static AnimalDto animalPayload(String name, String species, Long ownerId) {
        AnimalDto dto = new AnimalDto();
        dto.setName(name);
        dto.setSpecies(species);
        dto.setOwnerId(ownerId);
        return dto;
    }

    private static VisitDto visitPayload(LocalDate date, String reason, double age, double weight, double height) {
        VisitDto dto = new VisitDto();
        dto.setVisitDate(date);
        dto.setReason(reason);
        dto.setAge(age);
        dto.setWeight(weight);
        dto.setHeight(height);
        dto.setVetName("Dr. Test");
        return dto;
    }
}
