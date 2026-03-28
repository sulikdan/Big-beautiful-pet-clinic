package com.petclinic.service;

import com.petclinic.dto.AnimalDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Gender;
import com.petclinic.model.Owner;
import com.petclinic.model.Species;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private AnimalService animalService;

    private Owner owner;
    private Animal animal;
    private AnimalDto dto;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1L);
        owner.setFirstName("Alice");
        owner.setLastName("Johnson");

        animal = new Animal();
        animal.setId(1L);
        animal.setName("Buddy");
        animal.setSpecies(Species.DOG);
        animal.setBreed("Golden Retriever");
        animal.setDateOfBirth(LocalDate.of(2020, 3, 15));
        animal.setColor("Golden");
        animal.setGender(Gender.MALE);
        animal.setOwner(owner);

        dto = new AnimalDto();
        dto.setName("Buddy");
        dto.setSpecies("DOG");
        dto.setBreed("Golden Retriever");
        dto.setOwnerId(1L);
    }

    // ── search ──────────────────────────────────────────────────────────────

    @Test
    void search_noFilters_returnsAll() {
        when(animalRepository.search(null, null, null)).thenReturn(List.of(animal));

        var result = animalService.search(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Buddy");
    }

    @Test
    void search_withNameFilter_passesNameToRepo() {
        when(animalRepository.search("Buddy", null, null)).thenReturn(List.of(animal));

        var result = animalService.search("Buddy", null, null);

        assertThat(result).hasSize(1);
        verify(animalRepository).search("Buddy", null, null);
    }

    @Test
    void search_withSpeciesFilter_convertsToEnum() {
        when(animalRepository.search(null, Species.DOG, null)).thenReturn(List.of(animal));

        animalService.search(null, "DOG", null);

        verify(animalRepository).search(null, Species.DOG, null);
    }

    @Test
    void search_blankSpecies_passesNullEnum() {
        when(animalRepository.search(null, null, null)).thenReturn(List.of());

        animalService.search(null, "", null);

        verify(animalRepository).search(null, null, null);
    }

    @Test
    void search_invalidSpecies_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> animalService.search(null, "DRAGON", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_found_returnsMappedDto() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        var result = animalService.findById(1L);

        assertThat(result.getName()).isEqualTo("Buddy");
        assertThat(result.getSpecies()).isEqualTo("DOG");
        assertThat(result.getGender()).isEqualTo("MALE");
        assertThat(result.getOwnerName()).isEqualTo("Alice Johnson");
        assertThat(result.getOwnerId()).isEqualTo(1L);
    }

    @Test
    void findById_noOwner_ownerFieldsAreNull() {
        animal.setOwner(null);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        var result = animalService.findById(1L);

        assertThat(result.getOwnerId()).isNull();
        assertThat(result.getOwnerName()).isNull();
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_withOwner_loadsAndAssignsOwner() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        var result = animalService.create(dto);

        assertThat(result.getName()).isEqualTo("Buddy");
        verify(ownerRepository).findById(1L);
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    void create_withoutOwner_doesNotQueryOwnerRepository() {
        dto.setOwnerId(null);
        animal.setOwner(null);
        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        animalService.create(dto);

        verify(ownerRepository, never()).findById(any());
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    void create_ownerNotFound_throwsEntityNotFoundException() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.create(dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(animalRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_found_updatesAndSaves() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(animalRepository.save(animal)).thenReturn(animal);
        dto.setName("Rex");

        animalService.update(1L, dto);

        assertThat(animal.getName()).isEqualTo("Rex");
        verify(animalRepository).save(animal);
    }

    @Test
    void update_notFound_throwsEntityNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(animalRepository, never()).save(any());
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletesById() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        animalService.delete(1L);

        verify(animalRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(animalRepository, never()).deleteById(any());
    }
}
