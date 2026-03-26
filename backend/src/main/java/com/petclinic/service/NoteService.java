package com.petclinic.service;

import com.petclinic.dto.NoteDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Note;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    private final AnimalRepository animalRepository;

    public List<NoteDto> findByAnimalId(Long animalId) {
        return noteRepository.findByAnimalIdOrderByCreatedAtDesc(animalId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public NoteDto create(Long animalId, NoteDto dto) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));
        Note note = new Note();
        note.setAnimal(animal);
        note.setContent(dto.getContent());
        return toDto(noteRepository.save(note));
    }

    @Transactional
    public void delete(Long id) {
        noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found: " + id));
        noteRepository.deleteById(id);
    }

    private NoteDto toDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setAnimalId(note.getAnimal().getId());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}
