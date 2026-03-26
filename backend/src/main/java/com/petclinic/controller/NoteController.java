package com.petclinic.controller;

import com.petclinic.dto.NoteDto;
import com.petclinic.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/api/animals/{animalId}/notes")
    public List<NoteDto> getByAnimal(@PathVariable Long animalId) {
        return noteService.findByAnimalId(animalId);
    }

    @PostMapping("/api/animals/{animalId}/notes")
    public ResponseEntity<NoteDto> create(@PathVariable Long animalId,
                                          @Valid @RequestBody NoteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(animalId, dto));
    }

    @DeleteMapping("/api/notes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
