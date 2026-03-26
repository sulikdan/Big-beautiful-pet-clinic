package com.petclinic.controller;

import com.petclinic.dto.VisitDto;
import com.petclinic.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @GetMapping("/api/animals/{animalId}/visits")
    public List<VisitDto> getByAnimal(@PathVariable Long animalId) {
        return visitService.findByAnimalId(animalId);
    }

    @PostMapping("/api/animals/{animalId}/visits")
    public ResponseEntity<VisitDto> create(@PathVariable Long animalId,
                                           @Valid @RequestBody VisitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.create(animalId, dto));
    }

    @GetMapping("/api/visits/{id}")
    public VisitDto getById(@PathVariable Long id) {
        return visitService.findById(id);
    }

    @PutMapping("/api/visits/{id}")
    public VisitDto update(@PathVariable Long id, @Valid @RequestBody VisitDto dto) {
        return visitService.update(id, dto);
    }

    @DeleteMapping("/api/visits/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        visitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
