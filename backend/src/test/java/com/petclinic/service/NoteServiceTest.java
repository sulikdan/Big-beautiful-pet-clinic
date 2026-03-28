package com.petclinic.service;

import com.petclinic.dto.NoteDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Note;
import com.petclinic.model.Species;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private NoteService noteService;

    private Animal animal;
    private Note note;
    private NoteDto dto;

    @BeforeEach
    void setUp() {
        animal = new Animal();
        animal.setId(1L);
        animal.setName("Buddy");
        animal.setSpecies(Species.DOG);

        note = new Note();
        note.setId(1L);
        note.setAnimal(animal);
        note.setContent("Buddy loves fetch.");
        note.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 30));

        dto = new NoteDto();
        dto.setContent("Buddy loves fetch.");
    }

    @Test
    void findByAnimalId_returnsMappedDtos() {
        when(noteRepository.findByAnimalIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(note));

        var result = noteService.findByAnimalId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Buddy loves fetch.");
        assertThat(result.get(0).getAnimalId()).isEqualTo(1L);
        assertThat(result.get(0).getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 10, 10, 30));
    }

    @Test
    void findByAnimalId_noNotes_returnsEmptyList() {
        when(noteRepository.findByAnimalIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        var result = noteService.findByAnimalId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void create_animalFound_savesNoteAndReturnsDto() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        var result = noteService.create(1L, dto);

        assertThat(result.getContent()).isEqualTo("Buddy loves fetch.");
        assertThat(result.getAnimalId()).isEqualTo(1L);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void create_setsContentFromDto() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> {
            Note saved = inv.getArgument(0);
            saved.setId(99L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });
        dto.setContent("A specific note");

        var result = noteService.create(1L, dto);

        assertThat(result.getContent()).isEqualTo("A specific note");
    }

    @Test
    void create_animalNotFound_throwsEntityNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(noteRepository, never()).save(any());
    }

    @Test
    void delete_found_deletesById() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.delete(1L);

        verify(noteRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(noteRepository, never()).deleteById(any());
    }
}
