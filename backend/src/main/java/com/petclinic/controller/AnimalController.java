package com.petclinic.controller;

import com.petclinic.dto.AnimalDto;
import com.petclinic.service.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    public List<AnimalDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Long ownerId) {
        return animalService.search(name, species, ownerId);
    }

    @GetMapping("/{id}")
    public AnimalDto getById(@PathVariable Long id) {
        return animalService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AnimalDto> create(@Valid @RequestBody AnimalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(dto));
    }

    @PutMapping("/{id}")
    public AnimalDto update(@PathVariable Long id, @Valid @RequestBody AnimalDto dto) {
        return animalService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
