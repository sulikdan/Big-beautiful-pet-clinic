package com.petclinic.service;

import com.petclinic.dto.VisitDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Species;
import com.petclinic.model.Visit;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.VisitRepository;
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
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private VisitService visitService;

    private Animal animal;
    private Visit visit;
    private VisitDto dto;

    @BeforeEach
    void setUp() {
        animal = new Animal();
        animal.setId(1L);
        animal.setName("Buddy");
        animal.setSpecies(Species.DOG);

        visit = new Visit();
        visit.setId(1L);
        visit.setAnimal(animal);
        visit.setVisitDate(LocalDate.of(2024, 1, 10));
        visit.setReason("Annual check-up");
        visit.setWeight(30.0);
        visit.setHeight(65.0);
        visit.setAge(3.8);
        visit.setVetName("Dr. Evans");
        visit.setDiagnosis("Healthy");
        visit.setTreatment("Vaccines updated");

        dto = new VisitDto();
        dto.setVisitDate(LocalDate.of(2024, 1, 10));
        dto.setReason("Annual check-up");
        dto.setWeight(30.0);
        dto.setHeight(65.0);
        dto.setAge(3.8);
        dto.setVetName("Dr. Evans");
    }

    @Test
    void findByAnimalId_returnsMappedDtos() {
        when(visitRepository.findByAnimalIdOrderByVisitDateDesc(1L)).thenReturn(List.of(visit));

        var result = visitService.findByAnimalId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReason()).isEqualTo("Annual check-up");
        assertThat(result.get(0).getAnimalName()).isEqualTo("Buddy");
        assertThat(result.get(0).getAnimalId()).isEqualTo(1L);
        assertThat(result.get(0).getWeight()).isEqualTo(30.0);
    }

    @Test
    void findByAnimalId_noVisits_returnsEmptyList() {
        when(visitRepository.findByAnimalIdOrderByVisitDateDesc(1L)).thenReturn(List.of());

        var result = visitService.findByAnimalId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_found_returnsMappedDto() {
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        var result = visitService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVetName()).isEqualTo("Dr. Evans");
        assertThat(result.getDiagnosis()).isEqualTo("Healthy");
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_animalFound_savesVisitWithAnimal() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        var result = visitService.create(1L, dto);

        assertThat(result.getAnimalId()).isEqualTo(1L);
        assertThat(result.getAnimalName()).isEqualTo("Buddy");
        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void create_animalNotFound_throwsEntityNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.create(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void update_found_updatesAllFields() {
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);
        dto.setReason("Follow-up");
        dto.setDiagnosis("Recovered");

        visitService.update(1L, dto);

        assertThat(visit.getReason()).isEqualTo("Follow-up");
        assertThat(visit.getDiagnosis()).isEqualTo("Recovered");
        verify(visitRepository).save(visit);
    }

    @Test
    void update_notFound_throwsEntityNotFoundException() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void delete_found_deletesById() {
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        visitService.delete(1L);

        verify(visitRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(visitRepository, never()).deleteById(any());
    }
}
